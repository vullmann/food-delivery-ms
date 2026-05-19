package de.ullmann.fooddelivery.customerservice.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import de.ullmann.fooddelivery.common.model.Address;

class CustomerTest {

    @Test
    void create_shouldCreateCustomerWithCorrectValues() {
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");

        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "+49123456789",
                address
        );

        assertNotNull(customer);
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("password123", customer.getPassword());
        assertEquals("+49123456789", customer.getPhone());
        assertEquals(address, customer.getAddress());
        assertNotNull(customer.getId());
        assertNotNull(customer.getCreatedAt());
    }

    @Test
    void create_shouldCreateCustomerWithoutPhone() {
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");

        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                null,
                address
        );

        assertNotNull(customer);
        assertNull(customer.getPhone());
    }

    @Test
    void update_shouldUpdateCustomerFields() {
        Address oldAddress = Address.of("Old St", "1", "Berlin", "10115", "Germany");
        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "+49123456789",
                oldAddress
        );

        Address newAddress = Address.of("New St", "999", "Munich", "80331", "Germany");
        customer.update("Jane", "Smith", "+49987654321", newAddress);

        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("+49987654321", customer.getPhone());
        assertEquals(newAddress, customer.getAddress());
        assertEquals("john.doe@example.com", customer.getEmail());
        assertEquals("password123", customer.getPassword());
    }


    @Test
    void update_shouldUpdateAddress() {
        Address oldAddress = Address.of("Old St", "1", "Berlin", "10115", "Germany");
        Customer customer = Customer.create(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123",
                "+49123456789",
                oldAddress
        );

        Address newAddress = Address.of("New St", "999", "Munich", "80331", "Germany");
        customer.update("John", "Doe", "+49123456789", newAddress);

        assertEquals("New St", customer.getAddress().getStreet());
        assertEquals("999", customer.getAddress().getHouseNumber());
        assertEquals("Munich", customer.getAddress().getCity());
        assertEquals("80331", customer.getAddress().getZip());
        assertEquals("Germany", customer.getAddress().getCountry());
    }
}
