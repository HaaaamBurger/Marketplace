package com.marketplace.product.exception;

public class ProductAmountNotEnoughException extends RuntimeException {
    public ProductAmountNotEnoughException(String message) {
        super(message);
    }
}
