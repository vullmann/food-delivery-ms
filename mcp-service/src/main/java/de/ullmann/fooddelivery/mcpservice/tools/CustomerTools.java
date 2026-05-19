package de.ullmann.fooddelivery.mcpservice.tools;

import de.ullmann.fooddelivery.mcpservice.client.CustomerServiceClient;
import de.ullmann.fooddelivery.mcpservice.dto.CustomerResponse;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CustomerTools {

    private final CustomerServiceClient customerServiceClient;

    public CustomerTools(CustomerServiceClient customerServiceClient) {
        this.customerServiceClient = customerServiceClient;
    }

    @Tool(description = "Look up a customer's profile (name, email, phone, address) by their UUID.")
    public String getCustomer(String customerId) {
        return customerServiceClient.getCustomerById(customerId)
                .map(c -> "Customer ID: " + c.id() + "\nName: " + c.firstName() + " " + c.lastName()
                        + "\nEmail: " + c.email() + "\nPhone: " + c.phone()
                        + formatAddress(c.address()))
                .orElse("No customer found with ID: " + customerId);
    }

    @Tool(description = "Find a customer by their email address. " +
            "Use this to identify a customer when they don't know their customer ID.")
    public String findCustomerByEmail(String email) {
        return customerServiceClient.getCustomerByEmail(email)
                .map(c -> "Customer ID: " + c.id() + "\nName: " + c.firstName() + " " + c.lastName()
                        + "\nEmail: " + c.email() + "\nPhone: " + c.phone()
                        + formatAddress(c.address()))
                .orElse("No customer found with email: " + email);
    }

    private String formatAddress(CustomerResponse.Address a) {
        if (a == null) return "";
        return "\nAddress: " + a.street() + " " + a.houseNumber()
                + ", " + a.zip() + " " + a.city() + ", " + a.country();
    }
}
