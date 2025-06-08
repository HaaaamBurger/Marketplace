package com.marketplace.auth.security.cookie;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CookiePayload {

    private String name;

    private String value;

    private int maxAge;

}
