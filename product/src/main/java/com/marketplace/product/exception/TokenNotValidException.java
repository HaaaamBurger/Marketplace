package com.marketplace.product.exception;

import io.jsonwebtoken.JwtException;

public class TokenNotValidException extends JwtException {
    public TokenNotValidException(String message) {
        super(message);
    }
}
