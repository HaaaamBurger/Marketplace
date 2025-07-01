package com.marketplace.auth.service;

import com.marketplace.auth.security.cookie.CookiePayload;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.token.JwtService;
import com.marketplace.auth.security.token.TokenPayload;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;

@Service
@RequiredArgsConstructor
public class JwtCookieService {

    private final JwtService jwtService;

    private final CookieService cookieService;

    public void addTokensToCookie(TokenPayload tokenPayload, HttpServletResponse httpServletResponse) {
        CookiePayload accessTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_ACCESS_TOKEN)
                .value(tokenPayload.getAccessToken())
                .maxAge(jwtService.JWT_ACCESS_EXPIRATION_TIME)
                .build();
        CookiePayload refreshTokenCookiePayload = CookiePayload.builder()
                .name(COOKIE_REFRESH_TOKEN)
                .value(tokenPayload.getRefreshToken())
                .maxAge(jwtService.JWT_REFRESH_EXPIRATION_TIME)
                .build();

        cookieService.addPayloadToCookie(accessTokenCookiePayload, httpServletResponse);
        cookieService.addPayloadToCookie(refreshTokenCookiePayload, httpServletResponse);
    }

    public void deleteTokensFromCookie(HttpServletResponse response) {
        cookieService.deleteCookieByName(COOKIE_ACCESS_TOKEN, response);
        cookieService.deleteCookieByName(COOKIE_REFRESH_TOKEN, response);
    }
}
