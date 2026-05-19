package de.ullmann.fooddelivery.customerservice.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.ullmann.fooddelivery.common.event.CustomerCreatedEvent;
import de.ullmann.fooddelivery.common.event.CustomerProfileUpdatedEvent;
import de.ullmann.fooddelivery.common.model.Address;
import de.ullmann.fooddelivery.common.outbox.OutboxEventService;
import de.ullmann.fooddelivery.customerservice.dto.AddressRequest;
import de.ullmann.fooddelivery.customerservice.dto.CreateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.dto.UpdateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.entity.Customer;
import de.ullmann.fooddelivery.customerservice.exception.CustomerNotFoundException;
import de.ullmann.fooddelivery.customerservice.exception.EmailAlreadyInUseException;
import de.ullmann.fooddelivery.customerservice.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OutboxEventService outboxEventService;

    // --- Commands ---
    public Customer createCustomer(CreateCustomerRequest req) {
        if (customerRepository.findByEmail(req.email()).isPresent()) {
            throw new EmailAlreadyInUseException(req.email());
        }
        Address address = toAddress(req.address());
        Customer customer = Customer.create(
                req.firstName(), req.lastName(),
                req.email(), req.password(),
                req.phone(), address
        );
        customer = customerRepository.save(customer);
        publishCustomerCreated(customer);
        return customer;
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
                new CustomerProfileUpdatedEvent(customer.getId(), customer.getPhone(), LocalDateTime.now())
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