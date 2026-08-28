package de.ullmann.fooddelivery.orderservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

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

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceTest {

    @InjectMocks
    private CustomerOrderService customerOrderService;

    @Mock
    private CustomerOrderRepository customerOrderRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @Captor
    private ArgumentCaptor<CustomerOrder> orderCaptor;

    private CreateCustomerOrderRequest createCustomerOrderRequest;
    private UUID customerId;
    private UUID restaurantId;

    @BeforeEach
    void setUp() {

        customerId = UUID.randomUUID();
        restaurantId = UUID.randomUUID();

        createCustomerOrderRequest = new CreateCustomerOrderRequest(
                customerId,
                restaurantId,
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(
                        new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "with pepper", 2,
                                new BigDecimal("10.00")),
                        new CustomerOrderItemRequest(UUID.randomUUID(), "Pasta", "small", 1, new BigDecimal("15.00"))
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UUID principalId, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                principalId.toString(), null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
    }

    @Test
    void placeOrder_shouldCreateAndSaveOrder() {
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        CustomerOrder result = customerOrderService.placeOrder(createCustomerOrderRequest);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(restaurantId, result.getRestaurantId());
        assertEquals(new BigDecimal("35.00"), result.getTotalAmount());
        assertEquals(CustomerOrderStatus.PENDING, result.getStatus());
        assertEquals(2, result.getItems().size());

        verify(customerOrderRepository, times(1)).save(orderCaptor.capture());
        CustomerOrder savedCustomerOrder = orderCaptor.getValue();
        assertEquals(customerId, savedCustomerOrder.getCustomerId());
        assertEquals(restaurantId, savedCustomerOrder.getRestaurantId());
    }

    @Test
    void placeOrder_shouldMapItemsCorrectly() {
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(
                invocation -> invocation.getArgument(0));

        CustomerOrder result = customerOrderService.placeOrder(createCustomerOrderRequest);

        assertEquals(2, result.getItems().size());
        assertTrue(result.getItems().stream().anyMatch(item ->
                item.getName().equals("Pizza") && item.getQuantity() == 2));
        assertTrue(result.getItems().stream().anyMatch(item ->
                item.getName().equals("Pasta") && item.getQuantity() == 1));
    }

    @Test
    void updateStatus_shouldUpdateOrderStatus() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder customerOrder = createTestOrder(customerId, restaurantId);

        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(customerOrder));

        CustomerOrder result = customerOrderService.updateStatus(orderId, CustomerOrderStatus.CONFIRMED);

        assertEquals(CustomerOrderStatus.CONFIRMED, result.getStatus());
        verify(customerOrderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateStatus_shouldThrowExceptionWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();

        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(CustomerOrderNotFoundException.class,
                () -> customerOrderService.updateStatus(orderId, CustomerOrderStatus.CONFIRMED));

        verify(customerOrderRepository, times(1)).findById(orderId);
    }

    @Test
    void updateStatus_shouldThrowExceptionForInvalidTransition() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder customerOrder = createTestOrder(customerId, restaurantId);

        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(customerOrder));

        assertThrows(IllegalStateException.class,
                () -> customerOrderService.updateStatus(orderId, CustomerOrderStatus.DELIVERED));
    }

    @Test
    void findOrder_shouldReturnOrder() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder customerOrder = createTestOrder(customerId, restaurantId);

        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(customerOrder));

        CustomerOrder result = customerOrderService.findOrder(orderId);

        assertNotNull(result);
        assertEquals(customerOrder.getCustomerId(), result.getCustomerId());
        assertEquals(customerOrder.getRestaurantId(), result.getRestaurantId());
        verify(customerOrderRepository, times(1)).findById(orderId);
    }

    @Test
    void findOrder_shouldThrowExceptionWhenOrderNotFound() {
        UUID orderId = UUID.randomUUID();

        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(CustomerOrderNotFoundException.class,
                () -> customerOrderService.findOrder(orderId));

        verify(customerOrderRepository, times(1)).findById(orderId);
    }

    @Test
    void findOrdersByCustomer_shouldReturnListOfOrders() {
        CustomerOrder customerOrder1 = createTestOrder(customerId, UUID.randomUUID());
        CustomerOrder customerOrder2 = createTestOrder(customerId, UUID.randomUUID());

        when(customerOrderRepository.findAllByCustomerId(customerId))
                .thenReturn(List.of(customerOrder1, customerOrder2));

        List<CustomerOrder> result = customerOrderService.findOrdersByCustomer(customerId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(o -> o.getCustomerId().equals(customerId)));
        verify(customerOrderRepository, times(1)).findAllByCustomerId(customerId);
    }

    @Test
    void findOrdersByCustomer_shouldReturnEmptyListWhenNoOrdersFound() {
        when(customerOrderRepository.findAllByCustomerId(customerId))
                .thenReturn(List.of());

        List<CustomerOrder> result = customerOrderService.findOrdersByCustomer(customerId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(customerOrderRepository, times(1)).findAllByCustomerId(customerId);
    }

    private CustomerOrder createTestOrder(
            UUID customerId,
            UUID restaurantId) {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                customerId,
                restaurantId,
                new AddressRequest("St", "1", "City", "12345", "Country"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Test Item", "description", 1,
                        new BigDecimal("10.00")))
        );

        return customerOrderService.placeOrder(request);
    }

    // Builds a CustomerOrder entity directly (bypassing placeOrder) so test fixtures aren't subject to
    // the authorization check under test.
    private CustomerOrder buildOrder(UUID customerId, UUID restaurantId) {
        return CustomerOrder.create(
                customerId,
                restaurantId,
                Address.of("St", "1", "City", "12345", "Country"),
                List.of(CustomerOrderItem.create(UUID.randomUUID(), "Test Item", "description", 1,
                        new BigDecimal("10.00"))));
    }

    // ── placeOrder authorization ─────────────────────────────────────────────

    @Test
    void placeOrder_shouldSucceed_whenCallerIsCustomerPlacingOwnOrder() {
        authenticateAs(customerId, Role.CUSTOMER.name());
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = customerOrderService.placeOrder(createCustomerOrderRequest);

        assertEquals(customerId, result.getCustomerId());
    }

    @Test
    void placeOrder_shouldThrow_whenCallerIsCustomerPlacingForAnotherCustomer() {
        authenticateAs(UUID.randomUUID(), Role.CUSTOMER.name());

        assertThrows(CustomerOrderAccessDeniedException.class,
                () -> customerOrderService.placeOrder(createCustomerOrderRequest));
    }

    @Test
    void placeOrder_shouldSucceed_whenCallerIsSuperAdmin() {
        authenticateAs(UUID.randomUUID(), Role.SUPER_ADMIN.name());
        when(customerOrderRepository.save(any(CustomerOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerOrder result = customerOrderService.placeOrder(createCustomerOrderRequest);

        assertEquals(customerId, result.getCustomerId());
    }

    @Test
    void placeOrder_shouldThrow_whenCallerIsNotCustomerOrSuperAdmin() {
        authenticateAs(UUID.randomUUID(), Role.DELIVERY_DRIVER.name());

        assertThrows(InsufficientRoleException.class,
                () -> customerOrderService.placeOrder(createCustomerOrderRequest));
    }

    // ── findOrder authorization ───────────────────────────────────────────────

    @Test
    void findOrder_shouldThrow_whenCallerIsADifferentCustomer() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder order = buildOrder(customerId, restaurantId);
        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        authenticateAs(UUID.randomUUID(), Role.CUSTOMER.name());

        assertThrows(CustomerOrderAccessDeniedException.class, () -> customerOrderService.findOrder(orderId));
    }

    @Test
    void findOrder_shouldSucceed_whenCallerIsTheOwningCustomer() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder order = buildOrder(customerId, restaurantId);
        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        authenticateAs(customerId, Role.CUSTOMER.name());

        CustomerOrder result = customerOrderService.findOrder(orderId);

        assertEquals(customerId, result.getCustomerId());
    }

    @Test
    void findOrder_shouldSucceed_whenCallerIsStaffViewingAnotherCustomersOrder() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder order = buildOrder(customerId, restaurantId);
        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        authenticateAs(UUID.randomUUID(), Role.RESTAURANT_ADMIN.name());

        CustomerOrder result = customerOrderService.findOrder(orderId);

        assertEquals(customerId, result.getCustomerId());
    }

    // ── findOrdersByCustomer authorization ───────────────────────────────────

    @Test
    void findOrdersByCustomer_shouldThrow_whenCallerIsADifferentCustomer() {
        authenticateAs(UUID.randomUUID(), Role.CUSTOMER.name());

        assertThrows(CustomerOrderAccessDeniedException.class,
                () -> customerOrderService.findOrdersByCustomer(customerId));
    }

    @Test
    void findOrdersByCustomer_shouldSucceed_whenCallerIsTheOwningCustomer() {
        when(customerOrderRepository.findAllByCustomerId(customerId)).thenReturn(List.of());
        authenticateAs(customerId, Role.CUSTOMER.name());

        List<CustomerOrder> result = customerOrderService.findOrdersByCustomer(customerId);

        assertNotNull(result);
    }

    // ── updateStatus authorization ────────────────────────────────────────────

    @Test
    void updateStatus_shouldThrow_whenCallerIsCustomer() {
        UUID orderId = UUID.randomUUID();
        authenticateAs(customerId, Role.CUSTOMER.name());

        assertThrows(InsufficientRoleException.class,
                () -> customerOrderService.updateStatus(orderId, CustomerOrderStatus.CONFIRMED));
    }

    @Test
    void updateStatus_shouldSucceed_whenCallerIsRestaurantEmployee() {
        UUID orderId = UUID.randomUUID();
        CustomerOrder order = buildOrder(customerId, restaurantId);
        order.transitionTo(CustomerOrderStatus.PENDING);
        when(customerOrderRepository.findById(orderId)).thenReturn(Optional.of(order));
        authenticateAs(UUID.randomUUID(), Role.RESTAURANT_EMPLOYEE.name());

        CustomerOrder result = customerOrderService.updateStatus(orderId, CustomerOrderStatus.CONFIRMED);

        assertEquals(CustomerOrderStatus.CONFIRMED, result.getStatus());
    }
}
