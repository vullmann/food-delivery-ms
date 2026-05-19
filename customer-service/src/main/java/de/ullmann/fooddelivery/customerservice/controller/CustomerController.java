package de.ullmann.fooddelivery.customerservice.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.ullmann.fooddelivery.customerservice.dto.CreateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.dto.CustomerResponse;
import de.ullmann.fooddelivery.customerservice.dto.UpdateCustomerRequest;
import de.ullmann.fooddelivery.customerservice.service.CustomerService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@RequestBody @Valid CreateCustomerRequest req) {
        return CustomerResponse.from(customerService.createCustomer(req));
    }

    @GetMapping("/{id}")
    public CustomerResponse findCustomer(@PathVariable UUID id) {
        return CustomerResponse.from(customerService.findCustomer(id));
    }

    @GetMapping(params = "email")
    public CustomerResponse findByEmail(@RequestParam String email) {
        return CustomerResponse.from(customerService.findCustomerByEmail(email));
    }

    @GetMapping(params = "!email") // Only matches if 'email' is absent
    public List<CustomerResponse> findAllCustomers() {
        return customerService.findAllCustomers().stream().map(CustomerResponse::from).toList();
    }

    @PutMapping("/{id}")
    public CustomerResponse updateCustomer(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateCustomerRequest req) {
        return CustomerResponse.from(customerService.updateCustomer(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
    }
}