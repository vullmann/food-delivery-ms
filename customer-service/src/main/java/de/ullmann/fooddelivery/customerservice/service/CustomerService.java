package de.ullmann.fooddelivery.customerservice.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.event.UserRegisteredEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.customerservice.dto.AddressRequest;
import de.ullmann.fooddelivery.customerservice.dto.UpdateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.entity.Customer;
import de.ullmann.fooddelivery.customerservice.exception.CustomerNotFoundException;
import de.ullmann.fooddelivery.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OutboxEventService outboxEventService;

    // --- Commands ---

    // Consumes UserRegisteredEvent (role=CUSTOMER) so the customer profile shares
    // its id with the auth-service userId; idempotent against redelivery.
    public void registerFromEvent(UserRegisteredEvent event) {
        if (customerRepository.existsById(event.userId())) {
            log.info("Customer profile for userId={} already exists, skipping", event.userId());
            return;
        }
        Customer customer = Customer.createWithId(
                event.userId(), event.firstName(), event.lastName(),
                event.email(), event.phone(), event.address()
        );
        customer = customerRepository.save(customer);
        publishCustomerCreated(customer);
    }

    public Customer updateCustomer(
            UUID customerId,
            UpdateCustomerRequest req) {
        Customer customer = findOrThrow(customerId);
        customer.update(req.firstName(), req.lastName(), req.phone(), toAddress(req.address()));
        outboxEventService.createEvent(
                "Customer",
                customer.getId(),
                CustomerProfileUpdatedEvent.TOPIC,
                new CustomerProfileUpdatedEvent(customer.getId(), customer.getPhone(), LocalDateTime.now(ZoneOffset.UTC))
        );
        return customer;
    }

    public void deleteCustomer(UUID customerId) {
        Customer customer = findOrThrow(customerId);
        customerRepository.delete(customer);
    }

    // --- Queries ---
    @Transactional(readOnly = true)
    public Customer findCustomer(UUID customerId) {
        return findOrThrow(customerId);
    }

    @Transactional(readOnly = true)
    public List<Customer> findAllCustomers() {
        return customerRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Customer findCustomerByEmail(String email) {
        return customerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomerNotFoundException(email));
    }

    // --- Private helpers ---
    private Customer findOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    private Address toAddress(AddressRequest r) {
        return Address.of(r.street(), r.houseNumber(), r.city(), r.zip(), r.country());
    }

    private void publishCustomerCreated(Customer customer) {
        CustomerCreatedEvent customerCreatedEvent = new CustomerCreatedEvent(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getAddress(),
                customer.getCreatedAt()
        );

        // create outbox event
        outboxEventService.createEvent(
                "Customer",
                customer.getId(),
                CustomerCreatedEvent.TOPIC,
                customerCreatedEvent
        );
    }
}