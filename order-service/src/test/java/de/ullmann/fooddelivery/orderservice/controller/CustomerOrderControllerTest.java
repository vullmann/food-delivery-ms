package de.ullmann.fooddelivery.orderservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.orderservice.dto.AddressRequest;
import de.ullmann.fooddelivery.orderservice.dto.CreateCustomerOrderRequest;
import de.ullmann.fooddelivery.orderservice.dto.CustomerOrderItemRequest;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrder;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderItem;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.exception.CustomerOrderNotFoundException;
import de.ullmann.fooddelivery.orderservice.exception.GlobalExceptionHandler;
import de.ullmann.fooddelivery.orderservice.service.CustomerOrderService;

// addFilters=false: this slice doesn't load the app's SecurityConfig, so without this the default
// Spring Security auto-configuration would require authentication on every request.
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(CustomerOrderController.class)
@Import(GlobalExceptionHandler.class)
class CustomerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CustomerOrderService customerOrderService;

    @Test
    void placeOrder_shouldReturnCreatedOrder() throws Exception {
        CreateCustomerOrderRequest request = new CreateCustomerOrderRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "the best Pizza", 1,
                        new BigDecimal("10.00")))
        );

        CustomerOrder customerOrder = createTestOrder(request);
        when(customerOrderService.placeOrder(any(CreateCustomerOrderRequest.class))).thenReturn(customerOrder);

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(customerOrder.getCustomerId().toString()))
                .andExpect(jsonPath("$.restaurantId").value(customerOrder.getRestaurantId().toString()))
                .andExpect(jsonPath("$.totalAmount").value(10.00))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void placeOrder_shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        CreateCustomerOrderRequest invalidRequest = new CreateCustomerOrderRequest(
                null,
                UUID.randomUUID(),
                new AddressRequest("Main St", "123", "Berlin", "10115", "Germany"),
                List.of(new CustomerOrderItemRequest(UUID.randomUUID(), "Pizza", "the best Pizza", 1,
                        new BigDecimal("10.00")))
        );

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOrder_shouldReturnOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        CustomerOrder customerOrder = createTestOrder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10.00")
        );

        when(customerOrderService.findOrder(orderId)).thenReturn(customerOrder);

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerOrder.getCustomerId().toString()))
                .andExpect(jsonPath("$.restaurantId").value(customerOrder.getRestaurantId().toString()))
                .andExpect(jsonPath("$.totalAmount").value(10.00))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    void getOrder_shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(customerOrderService.findOrder(orderId)).thenThrow(new CustomerOrderNotFoundException(orderId));

        mockMvc.perform(get("/orders/{id}", orderId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Order not found: " + orderId));
    }

    @Test
    void getByCustomer_shouldReturnListOfOrders() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerOrder customerOrder1 = createTestOrder(customerId, UUID.randomUUID(), new BigDecimal("10.00"));
        CustomerOrder customerOrder2 = createTestOrder(customerId, UUID.randomUUID(), new BigDecimal("20.00"));

        when(customerOrderService.findOrdersByCustomer(customerId)).thenReturn(
                Arrays.asList(customerOrder1, customerOrder2));

        mockMvc.perform(get("/orders/customer/{customerId}", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$[1].customerId").value(customerId.toString()));
    }

    @Test
    void getByCustomer_shouldReturnEmptyListWhenNoOrdersFound() throws Exception {
        UUID customerId = UUID.randomUUID();

        when(customerOrderService.findOrdersByCustomer(customerId)).thenReturn(List.of());

        mockMvc.perform(get("/orders/customer/{customerId}", customerId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateStatus_shouldReturnUpdatedOrder() throws Exception {
        UUID orderId = UUID.randomUUID();
        CustomerOrder customerOrder = createTestOrder(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("10.00")
        );
        customerOrder.transitionTo(CustomerOrderStatus.PENDING);
        customerOrder.transitionTo(CustomerOrderStatus.CONFIRMED);

        when(customerOrderService.updateStatus(eq(orderId), eq(CustomerOrderStatus.CONFIRMED))).thenReturn(
                customerOrder);

        mockMvc.perform(patch("/orders/{id}/status", orderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void updateStatus_shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(customerOrderService.updateStatus(eq(orderId), eq(CustomerOrderStatus.CONFIRMED)))
                .thenThrow(new CustomerOrderNotFoundException(orderId));

        mockMvc.perform(patch("/orders/{id}/status", orderId)
                        .param("status", "CONFIRMED"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_shouldReturnConflictWhenTransitionIsInvalid() throws Exception {
        UUID orderId = UUID.randomUUID();

        when(customerOrderService.updateStatus(orderId, CustomerOrderStatus.DELIVERED))
                .thenThrow(new IllegalStateException("Cannot transition from PENDING to DELIVERED"));

        mockMvc.perform(patch("/orders/{id}/status", orderId)
                        .param("status", "DELIVERED"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Cannot transition from PENDING to DELIVERED"));
    }

    private CustomerOrder createTestOrder(CreateCustomerOrderRequest request) {
        List<CustomerOrderItem> items = request.items().stream()
                .map(i -> CustomerOrderItem.create(i.menuItemId(), i.name(), i.description(), i.quantity(), i.price()))
                .toList();

        Address address = Address.of(
                request.deliveryAddress().street(),
                request.deliveryAddress().houseNumber(),
                request.deliveryAddress().city(),
                request.deliveryAddress().zip(),
                request.deliveryAddress().country()
        );

        return CustomerOrder.create(request.customerId(), request.restaurantId(), address, items);
    }

    private CustomerOrder createTestOrder(
            UUID customerId,
            UUID restaurantId,
            BigDecimal itemPrice) {
        List<CustomerOrderItem> items = List.of(
                CustomerOrderItem.create(UUID.randomUUID(), "Test Item", "the best Pizza", 1, itemPrice)
        );

        Address address = Address.of("St", "1", "City", "12345", "Country");

        return CustomerOrder.create(customerId, restaurantId, address, items);
    }
}
