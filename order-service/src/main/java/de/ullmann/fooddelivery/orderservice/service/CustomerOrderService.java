package de.ullmann.fooddelivery.orderservice.service;

import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.PENDING;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.common.security.Role;
import de.ullmann.fooddelivery.orderservice.dto.AddressRequest;
import de.ullmann.fooddelivery.orderservice.dto.CreateCustomerOrderRequest;
import de.ullmann.fooddelivery.orderservice.dto.CustomerOrderItemRequest;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.exception.CustomerOrderAccessDeniedException;
import de.ullmann.fooddelivery.orderservice.exception.CustomerOrderNotFoundException;
import de.ullmann.fooddelivery.orderservice.exception.InsufficientRoleException;
import de.ullmann.fooddelivery.orderservice.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerOrderService {

    private static final String SUPER_ADMIN_AUTHORITY = "ROLE_" + Role.SUPER_ADMIN;
    private static final String CUSTOMER_AUTHORITY = "ROLE_" + Role.CUSTOMER;
    private static final String RESTAURANT_ADMIN_AUTHORITY = "ROLE_" + Role.RESTAURANT_ADMIN;
    private static final String RESTAURANT_EMPLOYEE_AUTHORITY = "ROLE_" + Role.RESTAURANT_EMPLOYEE;
    private static final String DELIVERY_ADMIN_AUTHORITY = "ROLE_" + Role.DELIVERY_ADMIN;
    private static final String DELIVERY_DRIVER_AUTHORITY = "ROLE_" + Role.DELIVERY_DRIVER;

    private final CustomerOrderRepository customerOrderRepository;
    private final OutboxEventService outboxEventService;

    // --- Commands ---

    public CustomerOrder placeOrder(CreateCustomerOrderRequest req) {
        assertCallerMayPlaceOrderFor(req.customerId());

        // create the customer order
        List<CustomerOrderItem> items = req.items().stream()
                .map(this::toOrderItem)
                .toList();

        Address deliveryAddress = toAddress(req.deliveryAddress());

        CustomerOrder customerOrder = CustomerOrder.create(req.customerId(), req.restaurantId(), deliveryAddress, items);

        // save in DB
        customerOrder.transitionTo(PENDING);
        customerOrderRepository.save(customerOrder);


        List<OrderItemDto> itemDtos = customerOrder.getItems().stream()
                .map(i -> new OrderItemDto(i.getMenuItemId(), i.getName(),
                        i.getQuantity(), i.getTotalPrice()))
                .toList();

        // create OrderPlacedEvent event
        OrderPlacedEvent orderPlacedEvent = new OrderPlacedEvent(
                customerOrder.getId(), customerOrder.getCustomerId(),
                customerOrder.getRestaurantId(), customerOrder.getTotalAmount(), itemDtos,
                customerOrder.getDeliveryAddress(), customerOrder.getCreatedAt()
        );

        // create outbox event
        outboxEventService.createEvent(
                "CustomerOrder",
                customerOrder.getId(),
                OrderPlacedEvent.TOPIC,
                orderPlacedEvent
        );
        return customerOrder;
    }

    public CustomerOrder updateStatus(
            UUID id,
            CustomerOrderStatus newStatus) {
        assertCallerManagesOrders();
        CustomerOrder customerOrder = findOrThrow(id);
        customerOrder.transitionTo(newStatus);
        return customerOrder;
    }

    // --- Queries ---

    @Transactional(readOnly = true)
    public CustomerOrder findOrder(UUID id) {
        CustomerOrder customerOrder = findOrThrow(id);
        assertCallerOwnsOrder(customerOrder);
        return customerOrder;
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findOrdersByCustomer(UUID customerId) {
        assertCallerMayViewOrdersOf(customerId);
        return customerOrderRepository.findAllByCustomerId(customerId);
    }

    // --- Private helpers ---

    private CustomerOrder findOrThrow(UUID id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
    }

    // A CUSTOMER caller may only place an order for itself; SUPER_ADMIN may place an order on behalf of
    // any customer. No other role may place orders. No authentication in context (e.g. plain unit tests,
    // internal callers) is treated as unrestricted.
    private void assertCallerMayPlaceOrderFor(UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        if (hasAuthority(authentication, SUPER_ADMIN_AUTHORITY)) {
            return;
        }
        if (!hasAuthority(authentication, CUSTOMER_AUTHORITY)) {
            throw new InsufficientRoleException("Only customers may place orders");
        }
        if (!callerId(authentication).equals(customerId)) {
            throw new CustomerOrderAccessDeniedException(customerId);
        }
    }

    // A CUSTOMER caller may only view its own order; every other authenticated role may view any order.
    // No authentication in context is treated as unrestricted.
    private void assertCallerOwnsOrder(CustomerOrder order) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !hasAuthority(authentication, CUSTOMER_AUTHORITY)) {
            return;
        }
        if (!callerId(authentication).equals(order.getCustomerId())) {
            throw new CustomerOrderAccessDeniedException(order.getId());
        }
    }

    // A CUSTOMER caller may only list its own orders; every other authenticated role may list any
    // customer's orders. No authentication in context is treated as unrestricted.
    private void assertCallerMayViewOrdersOf(UUID customerId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !hasAuthority(authentication, CUSTOMER_AUTHORITY)) {
            return;
        }
        if (!callerId(authentication).equals(customerId)) {
            throw new CustomerOrderAccessDeniedException(customerId);
        }
    }

    // Order status may only be changed by staff (SUPER_ADMIN, RESTAURANT_ADMIN, RESTAURANT_EMPLOYEE,
    // DELIVERY_ADMIN, DELIVERY_DRIVER); a CUSTOMER may not directly change order status. No authentication
    // in context (e.g. internal Kafka consumer calls driving the order lifecycle) is treated as unrestricted.
    private void assertCallerManagesOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return;
        }
        boolean allowed = hasAuthority(authentication, SUPER_ADMIN_AUTHORITY)
                || hasAuthority(authentication, RESTAURANT_ADMIN_AUTHORITY)
                || hasAuthority(authentication, RESTAURANT_EMPLOYEE_AUTHORITY)
                || hasAuthority(authentication, DELIVERY_ADMIN_AUTHORITY)
                || hasAuthority(authentication, DELIVERY_DRIVER_AUTHORITY);
        if (!allowed) {
            throw new InsufficientRoleException("Only staff may change order status");
        }
    }

    private boolean hasAuthority(Authentication authentication, String authority) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private UUID callerId(Authentication authentication) {
        return UUID.fromString((String) authentication.getPrincipal());
    }

    private CustomerOrderItem toOrderItem(CustomerOrderItemRequest r) {
        return CustomerOrderItem.create(r.menuItemId(), r.name(), r.description(), r.quantity(), r.price());
    }

    private Address toAddress(AddressRequest r) {
        return Address.of(r.street(), r.houseNumber(), r.city(), r.zip(), r.country());
    }

    /*
    private void publishOrderPlaced(CustomerOrder customerOrder) {
        List<OrderItemDto> itemDtos = customerOrder.getItems().stream()
                .map(i -> new OrderItemDto(i.getMenuItemId(), i.getName(),
                        i.getQuantity(), i.getPrice()))
                .toList();

        OrderPlacedEvent event = new OrderPlacedEvent(
                customerOrder.getId(), customerOrder.getCustomerId(), customerOrder.getRestaurantId(),
                customerOrder.getTotalAmount(), itemDtos, customerOrder.getDeliveryAddress(),
                customerOrder.getCreatedAt()
        );

        OutboxEvent outboxEvent = new OutboxEvent(
                "CustomerOrder",
                customerOrder.getId(),
                "OrderPlaced",
                toJson(event)
        );


        kafkaTemplate.send("order-placed", customerOrder.getId().toString(), event);
    }

     */

}