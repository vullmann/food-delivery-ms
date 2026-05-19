package de.ullmann.fooddelivery.orderservice.service;

import static de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus.PENDING;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.OrderItemDto;
import de.ullmann.fooddelivery.common.event.OrderPlacedEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.orderservice.dto.AddressRequest;
import de.ullmann.fooddelivery.orderservice.dto.CreateCustomerOrderRequest;
import de.ullmann.fooddelivery.orderservice.dto.CustomerOrderItemRequest;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.exception.CustomerOrderNotFoundException;
import de.ullmann.fooddelivery.orderservice.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerOrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final OutboxEventService outboxEventService;

    // --- Commands ---

    public CustomerOrder placeOrder(CreateCustomerOrderRequest req) {
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
        CustomerOrder customerOrder = findOrThrow(id);
        customerOrder.transitionTo(newStatus);
        return customerOrder;
    }

    // --- Queries ---

    @Transactional(readOnly = true)
    public CustomerOrder findOrder(UUID id) {
        return findOrThrow(id);
    }

    @Transactional(readOnly = true)
    public List<CustomerOrder> findOrdersByCustomer(UUID customerId) {
        return customerOrderRepository.findAllByCustomerId(customerId);
    }

    // --- Private helpers ---

    private CustomerOrder findOrThrow(UUID id) {
        return customerOrderRepository.findById(id)
                .orElseThrow(() -> new CustomerOrderNotFoundException(id));
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