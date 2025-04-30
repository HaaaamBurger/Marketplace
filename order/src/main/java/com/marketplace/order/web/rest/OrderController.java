package com.marketplace.order.web.rest;

import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
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

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        return ResponseEntity.ok(orderService.createOrder(orderRequest));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(
            @PathVariable String orderId,
            @Valid @RequestBody OrderRequest orderRequest
    ) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, orderRequest));
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
    }
}
