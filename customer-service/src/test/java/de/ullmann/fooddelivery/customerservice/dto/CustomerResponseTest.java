package de.ullmann.fooddelivery.customerservice.dto;

import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.customerservice.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CustomerResponseTest {

    @Test
    void from_shouldMapCustomerToCustomerResponse() {
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");
        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "+49123456789",
                address
        );

        CustomerResponse response = CustomerResponse.from(customer);

        assertNotNull(response);
        assertEquals(customer.getId(), response.id());
        assertEquals("John", response.firstName());
        assertEquals("Doe", response.lastName());
        assertEquals("john.doe@example.com", response.email());
        assertEquals("+49123456789", response.phone());
        assertEquals(address, response.address());
        assertEquals(customer.getCreatedAt(), response.createdAt());
    }

    @Test
    void from_shouldMapCustomerWithoutPhone() {
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");
        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                null,
                address
        );

        CustomerResponse response = CustomerResponse.from(customer);

        assertNotNull(response);
        assertNull(response.phone());
    }

    @Test
    void from_shouldMapAllFields() {
        Address address = Address.of("Oak Ave", "456", "Munich", "80331", "Germany");
        Customer customer = Customer.create(
                "Jane",
                "Smith",
                "jane.smith@example.com",
                "securepass",
                "+4998765432",
                address
        );

        CustomerResponse response = CustomerResponse.from(customer);

        assertEquals("Jane", response.firstName());
        assertEquals("Smith", response.lastName());
        assertEquals("jane.smith@example.com", response.email());
        assertEquals("+4998765432", response.phone());
        assertEquals("Oak Ave", response.address().getStreet());
        assertEquals("456", response.address().getHouseNumber());
        assertEquals("Munich", response.address().getCity());
        assertEquals("80331", response.address().getZip());
        assertEquals("Germany", response.address().getCountry());
    }
}
