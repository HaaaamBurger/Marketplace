package com.marketplace.common.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ModelAttributes {
    REQUEST("request"),
    USER("user");

    private final String attributeName;
}


