package com.marketplace.order.service;

import com.marketplace.order.web.model.Order;

public interface OrderValidationService {

    void validateOrderUpdateOrThrow(Order order);

    void validateOrderAccessOrThrow(Order order);

}
