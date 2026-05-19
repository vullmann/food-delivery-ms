package de.ullmann.fooddelivery.orderservice.controller;


import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.ullmann.fooddelivery.orderservice.dto.CreateCustomerOrderRequest;
import de.ullmann.fooddelivery.orderservice.dto.CustomerOrderResponse;
import de.ullmann.fooddelivery.orderservice.entity.CustomerOrderStatus;
import de.ullmann.fooddelivery.orderservice.service.CustomerOrderService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOrderResponse placeOrder(@RequestBody @Valid CreateCustomerOrderRequest req) {
        return CustomerOrderResponse.from(customerOrderService.placeOrder(req));
    }

    @GetMapping("/{id}")
    public CustomerOrderResponse getOrder(@PathVariable UUID id) {
        return CustomerOrderResponse.from(customerOrderService.findOrder(id));
    }

    @GetMapping("/customer/{customerId}")
    public List<CustomerOrderResponse> getOrdersByCustomerId(@PathVariable UUID customerId) {
        return customerOrderService.findOrdersByCustomer(customerId).stream().map(CustomerOrderResponse::from).toList();
    }

    @PatchMapping("/{id}/status")
    public CustomerOrderResponse updateStatus(
            @PathVariable UUID id,
            @RequestParam CustomerOrderStatus status) {
        return CustomerOrderResponse.from(customerOrderService.updateStatus(id, status));
    }
}