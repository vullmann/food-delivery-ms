package de.ullmann.fooddelivery.mcpservice.tools;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.mcpservice.client.OrderServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.OrderResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderToolsTest {

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private OrderTools orderTools;

    private static final String ORDER_ID = "ord-id-123";
    private static final String CUSTOMER_ID = "cust-id-123";

    private OrderResponse buildOrder() {
        return new OrderResponse(ORDER_ID, CUSTOMER_ID, "rest-id", "PLACED",
                BigDecimal.valueOf(25.00), List.of(), LocalDateTime.now(ZoneOffset.UTC));
    }

    @Test
    void getOrderStatus_whenFound_shouldReturnFormattedInfo() {
        when(orderServiceClient.getOrderById(ORDER_ID)).thenReturn(Optional.of(buildOrder()));

        String result = orderTools.getOrderStatus(ORDER_ID);

        assertThat(result).contains(ORDER_ID).contains("PLACED");
    }

    @Test
    void getOrderStatus_whenNotFound_shouldReturnNotFoundMessage() {
        when(orderServiceClient.getOrderById(ORDER_ID)).thenReturn(Optional.empty());

        String result = orderTools.getOrderStatus(ORDER_ID);

        assertThat(result).contains("No order found").contains(ORDER_ID);
    }

    @Test
    void getOrdersByCustomer_whenOrdersExist_shouldReturnFormattedList() {
        when(orderServiceClient.getOrdersByCustomer(CUSTOMER_ID)).thenReturn(List.of(buildOrder(), buildOrder()));

        String result = orderTools.getOrdersByCustomer(CUSTOMER_ID);

        assertThat(result).contains(CUSTOMER_ID).contains("Order 1").contains("Order 2");
    }

    @Test
    void getOrdersByCustomer_whenNoOrders_shouldReturnNoOrdersMessage() {
        when(orderServiceClient.getOrdersByCustomer(CUSTOMER_ID)).thenReturn(List.of());

        String result = orderTools.getOrdersByCustomer(CUSTOMER_ID);

        assertThat(result).contains("No orders found").contains(CUSTOMER_ID);
    }
}
