package de.ullmann.fooddelivery.mcpservice.tools;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.mcpservice.client.DeliveryServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.DeliveryResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliveryToolsTest {

    @Mock
    private DeliveryServiceClient deliveryServiceClient;

    @InjectMocks
    private DeliveryTools deliveryTools;

    private static final String ORDER_ID = "ord-id-123";

    @Test
    void getDeliveryStatus_whenFound_shouldReturnFormattedInfo() {
        DeliveryResponse response = new DeliveryResponse("del-id", ORDER_ID, "PENDING", "drv-id",
                LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.now(ZoneOffset.UTC));
        when(deliveryServiceClient.getDeliveryByOrderId(ORDER_ID)).thenReturn(Optional.of(response));

        String result = deliveryTools.getDeliveryStatus(ORDER_ID);

        assertThat(result).contains("del-id").contains(ORDER_ID).contains("PENDING");
    }

    @Test
    void getDeliveryStatus_whenNotFound_shouldReturnNotFoundMessage() {
        when(deliveryServiceClient.getDeliveryByOrderId(ORDER_ID)).thenReturn(Optional.empty());

        String result = deliveryTools.getDeliveryStatus(ORDER_ID);

        assertThat(result).contains("No delivery found").contains(ORDER_ID);
    }
}
