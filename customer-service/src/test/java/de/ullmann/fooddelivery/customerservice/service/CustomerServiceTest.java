package de.ullmann.fooddelivery.customerservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.customerservice.dto.AddressRequest;
import de.ullmann.fooddelivery.customerservice.dto.UpdateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.entity.Customer;
import de.ullmann.fooddelivery.customerservice.exception.CustomerNotFoundException;
import de.ullmann.fooddelivery.customerservice.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String JOHN = "John";
    private static final String MAIL = "john.doe@example.com";
    private static final String DOE = "Doe";
    private static final String PHONE = "+49123456789";

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private OutboxEventService outboxEventService;

    @InjectMocks
    private CustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @Test
    void registerFromEvent_shouldCreateCustomerWithEventUserId() {
        UUID userId = UUID.randomUUID();
        Address address = Address.of("Main St", "123", "Berlin", "10115", "Germany");
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId, "CUSTOMER", JOHN, DOE, MAIL, PHONE, address, LocalDateTime.now(ZoneOffset.UTC));

        when(customerRepository.existsById(userId)).thenReturn(false);
        when(customerRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        customerService.registerFromEvent(event);

        verify(customerRepository).save(customerCaptor.capture());
        Customer saved = customerCaptor.getValue();
        assertEquals(userId, saved.getId());
        assertEquals(JOHN, saved.getFirstName());
        assertEquals(MAIL, saved.getEmail());
    }

    @Test
    void registerFromEvent_shouldSkipWhenCustomerAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId, "CUSTOMER", JOHN, DOE, MAIL, PHONE,
                Address.of("Main St", "123", "Berlin", "10115", "Germany"), LocalDateTime.now(ZoneOffset.UTC));

        when(customerRepository.existsById(userId)).thenReturn(true);

        customerService.registerFromEvent(event);

        verify(customerRepository, never()).save(any());
    }

    @Test
    void updateCustomer_shouldUpdateCustomerFields() {
        UUID customerId = UUID.randomUUID();
        Customer existingCustomer = Customer.create(
                JOHN,
                DOE,
                MAIL,
                PHONE,
                Address.of("Old St", "1", "Berlin", "10115", "Germany")
        );

        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Jane",
                "Smith",
                "+49987654321",
                new AddressRequest("New St", "456", "Munich", "80331", "Germany")
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(existingCustomer));

        Customer result = customerService.updateCustomer(customerId, updateRequest);

        assertEquals("Jane", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("+49987654321", result.getPhone());
        assertEquals("New St", result.getAddress().getStreet());
        assertEquals("456", result.getAddress().getHouseNumber());
        assertEquals("Munich", result.getAddress().getCity());
        assertEquals("80331", result.getAddress().getZip());
        assertEquals("Germany", result.getAddress().getCountry());
        assertEquals(MAIL, result.getEmail());

        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void updateCustomer_shouldThrowExceptionWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();
        UpdateCustomerRequest updateRequest = new UpdateCustomerRequest(
                "Jane",
                "Smith",
                "+49987654321",
                new AddressRequest("New St", "456", "Munich", "80331", "Germany")
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(customerId, updateRequest));

        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void deleteCustomer_shouldDeleteCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.create(
                JOHN,
                DOE,
                MAIL,
                PHONE,
                Address.of("Main St", "123", "Berlin", "10115", "Germany")
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(customerId);

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, times(1)).delete(customer);
    }

    @Test
    void deleteCustomer_shouldThrowExceptionWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(customerId));

        verify(customerRepository, times(1)).findById(customerId);
        verify(customerRepository, never()).delete(any());
    }

    @Test
    void getCustomer_shouldReturnCustomer() {
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.create(
                JOHN,
                DOE,
                MAIL,
                PHONE,
                Address.of("Main St", "123", "Berlin", "10115", "Germany")
        );

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        Customer result = customerService.findCustomer(customerId);

        assertNotNull(result);
        assertEquals(JOHN, result.getFirstName());
        assertEquals(DOE, result.getLastName());
        assertEquals(MAIL, result.getEmail());
        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void findCustomer_shouldThrowExceptionWhenCustomerNotFound() {
        UUID customerId = UUID.randomUUID();

        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.findCustomer(customerId));

        verify(customerRepository, times(1)).findById(customerId);
    }

    @Test
    void getCustomerByEmail_shouldReturnCustomer() {
        String email = MAIL;
        Customer customer = Customer.create(
                JOHN,
                DOE,
                email,
                PHONE,
                Address.of("Main St", "123", "Berlin", "10115", "Germany")
        );

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(customer));

        Customer result = customerService.findCustomerByEmail(email);

        assertNotNull(result);
        assertEquals(JOHN, result.getFirstName());
        assertEquals(DOE, result.getLastName());
        assertEquals(email, result.getEmail());
        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    void findCustomerByEmail_shouldThrowExceptionWhenCustomerNotFound() {
        String email = "nonexistent@example.com";

        when(customerRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.findCustomerByEmail(email));

        verify(customerRepository, times(1)).findByEmail(email);
    }

    @Test
    void findAllCustomers_shouldReturnAllCustomers() {
        Customer c1 = Customer.create(JOHN, DOE, MAIL, PHONE,
                Address.of("Main St", "123", "Berlin", "10115", "Germany"));
        Customer c2 = Customer.create("Jane", "Smith", "jane@example.com", "+49987654321",
                Address.of("Oak Ave", "456", "Munich", "80331", "Germany"));

        when(customerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Customer> result = customerService.findAllCustomers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
    }
}
