package de.ullmann.fooddelivery.mcpservice.config;

import de.ullmann.fooddelivery.mcpservice.tools.CustomerTools;
import de.ullmann.fooddelivery.mcpservice.tools.DeliveryTools;
import de.ullmann.fooddelivery.mcpservice.tools.OrderTools;
import de.ullmann.fooddelivery.mcpservice.tools.RestaurantTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsConfig {

    @Bean
    public ToolCallbackProvider foodDeliveryTools(
            OrderTools orderTools,
            CustomerTools customerTools,
            DeliveryTools deliveryTools,
            RestaurantTools restaurantTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(orderTools, customerTools, deliveryTools, restaurantTools)
                .build();
    }
}
