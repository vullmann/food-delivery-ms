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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.orderservice.dto.AddressRequest;
import de.ullmann.fooddelivery.orderservice.dto.CreateCustomerOrderRequest;
import de.ullmann.fooddelivery.orderservice.dto.CustomerOrderItemRequest;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.exception.CustomerOrderNotFoundException;
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
}
