package com.marketplace.order.service.impl;

import com.marketplace.auth.repository.UserRepository;
import com.marketplace.common.exception.EntityNotFoundException;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final UserRepository userRepository;

    @Override
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        String userId = getCurrentUserId();

        Order saved = orderRepository.save(Order.builder()
                .userId(userId)
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build());

        return mapToResponse(saved);
    }

    @Override
    public OrderResponse getOrderById(String id) {
        return mapToResponse(findOrThrow(id));
    }

    @Override
    public OrderResponse updateOrder(String id, OrderRequest request) {
        Order order = findOrThrow(id);

        Optional.ofNullable(request.getProductIds()).ifPresent(order::setProductIds);
        Optional.ofNullable(request.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(request.getStatus()).ifPresent(order::setStatus);

        return mapToResponse(orderRepository.save(order));
    }

    @Override
    public void deleteOrder(String id) {
        orderRepository.delete(findOrThrow(id));
    }

    private Order findOrThrow(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    private String getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            String email = userDetails.getUsername();
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("User not found"))
                    .getId();
        } else {
            throw new IllegalStateException("User is not authenticated");
        }
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .productIds(order.getProductIds())
                .address(order.getAddress())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime() : null)
                .build();
    }
}
