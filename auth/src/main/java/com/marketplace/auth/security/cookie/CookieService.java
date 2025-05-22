package com.marketplace.auth.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CookieService {

    public static final String COOKIE_ACCESS_TOKEN = "accessToken";

    public static final String COOKIE_REFRESH_TOKEN = "refreshToken";

    public void addPayloadToCookie(CookiePayload cookiePayload, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookiePayload.getName(), cookiePayload.getValue());
        cookie.setMaxAge(cookiePayload.getMaxAge());
        configureCookie(cookie, response);
    }

    public Cookie extractCookieByName(String name, HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            throw new CookieNotFoundException("Cookies are not present");
        }

        return Arrays.stream(cookies)
                .filter(cookie -> cookie.getName().equals(name) && cookie.getValue() != null)
                .findFirst().orElseThrow(() -> new CookieNotFoundException("No cookie present by name: " + name));
    }

    public void deleteCookieByName(String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        configureCookie(cookie, response);
    }

    private void configureCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

}
