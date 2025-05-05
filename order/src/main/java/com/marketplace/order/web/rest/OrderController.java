package com.marketplace.order.web.rest;

import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
import com.marketplace.order.web.rest.util.OrderEntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    private final OrderEntityMapper orderEntityMapper;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<Order> orders = orderService.findAll();
        return ResponseEntity.ok(orderEntityMapper.mapEntitiesToResponseDtos(orders));
    }

    @PostMapping("/products")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        Order order = orderService.create(orderRequest);
        return ResponseEntity.ok(orderEntityMapper.mapEntityToResponseDto(order));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        Order order = orderService.findById(orderId);
        return ResponseEntity.ok(orderEntityMapper.mapEntityToResponseDto(order));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody OrderRequest orderRequest
    ) {
        Order order = orderService.update(orderId, orderRequest);
        return ResponseEntity.ok(orderEntityMapper.mapEntityToResponseDto(order));
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<OrderResponse> addProductToOrder(@PathVariable String productId) {
        Order order = orderService.addProductToOrder(productId);
        return ResponseEntity.ok(orderEntityMapper.mapEntityToResponseDto(order));
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable String orderId) {
        orderService.delete(orderId);
    }

}
