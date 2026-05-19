package de.ullmann.fooddelivery.mcpservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;

import de.ullmann.fooddelivery.mcpservice.client.CustomerServiceClient;
import de.ullmann.fooddelivery.mcpservice.client.DeliveryServiceClient;
import de.ullmann.fooddelivery.mcpservice.client.OrderServiceClient;
import de.ullmann.fooddelivery.mcpservice.client.RestaurantServiceClient;
import de.ullmann.fooddelivery.mcpservice.tools.CustomerTools;
import de.ullmann.fooddelivery.mcpservice.tools.DeliveryTools;
import de.ullmann.fooddelivery.mcpservice.tools.OrderTools;
import de.ullmann.fooddelivery.mcpservice.tools.RestaurantTools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class McpToolsConfigTest {

    @Test
    void foodDeliveryTools_shouldReturnProvider() {
        McpToolsConfig config = new McpToolsConfig();
        OrderTools orderTools = new OrderTools(mock(OrderServiceClient.class));
        CustomerTools customerTools = new CustomerTools(mock(CustomerServiceClient.class));
        DeliveryTools deliveryTools = new DeliveryTools(mock(DeliveryServiceClient.class));
        RestaurantTools restaurantTools = new RestaurantTools(mock(RestaurantServiceClient.class));

        ToolCallbackProvider provider = config.foodDeliveryTools(orderTools, customerTools, deliveryTools, restaurantTools);

        assertThat(provider).isNotNull();
    }
}
