package com.marketplace.order.service.impl;

import com.marketplace.auth.util.AuthHelper;
import com.marketplace.auth.web.model.User;
import com.marketplace.order.repository.OrderRepository;
import com.marketplace.order.service.OrderService;
import com.marketplace.order.web.model.Order;
import com.marketplace.order.web.model.OrderStatus;
import com.marketplace.order.web.rest.dto.OrderRequest;
import com.marketplace.order.web.rest.util.OrderServiceUtil;
import com.marketplace.product.web.rest.util.ProductServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    private final AuthHelper authHelper;

    private final OrderServiceUtil orderServiceUtil;

    private final ProductServiceUtil productServiceUtil;

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public Order create(OrderRequest request) {
        User authenticatedUser = authHelper.getAuthenticatedUser();

        List<String> productIds = request.getProductIds();
        productIds.forEach(productServiceUtil::findProductOrThrow);

        return orderRepository.save(Order.builder()
                .ownerId(authenticatedUser.getId())
                .productIds(request.getProductIds())
                .address(request.getAddress())
                .status(request.getStatus())
                .build());
    }

    @Override
    public Order findById(String orderId) {
        return orderServiceUtil.validateOrderAccessOrThrow(orderId);
    }

    @Override
    public Order update(String orderId, OrderRequest request) {
        Order order = orderServiceUtil.findOrderOrThrow(orderId);

        Optional.ofNullable(request.getProductIds()).ifPresent(order::setProductIds);
        Optional.ofNullable(request.getAddress()).ifPresent(order::setAddress);
        Optional.ofNullable(request.getStatus()).ifPresent(order::setStatus);

        return orderRepository.save(order);
    }

    @Override
    public void delete(String orderId) {
        Order order = orderServiceUtil.validateOrderAccessOrThrow(orderId);
        orderRepository.delete(order);
    }

    @Override
    public Order addProductToOrder(String productId) {
        productServiceUtil.findProductOrThrow(productId);
        Order order = orderServiceUtil.findOrderOrCreate();

        order.getProductIds().add(productId);
        order.setStatus(OrderStatus.IN_PROGRESS);

        return orderRepository.save(order);
    }

}
