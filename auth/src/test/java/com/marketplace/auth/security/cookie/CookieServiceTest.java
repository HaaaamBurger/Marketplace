package com.marketplace.auth.security.cookie;

import com.marketplace.auth.config.AuthApplicationConfig;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthApplicationConfig.class)
public class CookieServiceTest {

    @Autowired
    private CookieService cookieService;

    @Test
    public void addPayloadToCookie_ShouldAddPayloadToCookie() {
        HttpServletResponse mockedHttpServletResponse = mock(HttpServletResponse.class);
        CookiePayload cookiePayload = CookiePayload.builder()
                .name("testName")
                .value("testValue")
                .maxAge(100000)
                .build();

        cookieService.addPayloadToCookie(cookiePayload, mockedHttpServletResponse);

        verify(mockedHttpServletResponse, times(1)).addCookie(argThat(cookie ->
            cookie.getName().equals(cookiePayload.getName()) &&
            cookie.getValue().equals(cookiePayload.getValue()) &&
            cookie.getSecure() && cookie.isHttpOnly() &&
            cookie.getPath().equals("/")
        ));
    }

    @Test
    public void extractCookieByName_ShouldExtractCookieByName() {
        HttpServletRequest mockedHttpServletRequest = mock(HttpServletRequest.class);
        String cookieName = "testName";
        String cookieValue = "testValue";
        Cookie[] cookies = new Cookie[]{new Cookie(cookieName, cookieValue)};

        when(mockedHttpServletRequest.getCookies()).thenReturn(cookies);

        Cookie cookie = cookieService.extractCookieByName(cookieName, mockedHttpServletRequest);

        assertThat(cookie).isNotNull();
    }

    @Test
    public void extractCookieByName_ShouldThrowException_WhenCookiesNotPresent() {
        HttpServletRequest mockedHttpServletRequest = mock(HttpServletRequest.class);
        String cookieName = "testName";

        assertThatThrownBy(() -> cookieService.extractCookieByName(cookieName, mockedHttpServletRequest))
                .isInstanceOf(CookieNotFoundException.class)
                .hasMessage("Cookies are not present");
    }

    @Test
    public void extractCookieByName_ShouldThrowException_WhenCookieNotFoundByName() {
        HttpServletRequest mockedHttpServletRequest = mock(HttpServletRequest.class);
        String cookieName = "testName";
        String cookieName1 = "testName1";
        String cookieValue = "testValue";

        Cookie[] cookies = new Cookie[]{new Cookie(cookieName1, cookieValue)};

        when(mockedHttpServletRequest.getCookies()).thenReturn(cookies);
        assertThatThrownBy(() -> cookieService.extractCookieByName(cookieName, mockedHttpServletRequest))
                .isInstanceOf(CookieNotFoundException.class)
                .hasMessage("No cookie present by name: " + cookieName);
    }

    @Test
    public void deleteCookieByName_ShouldDeleteCookieByName() {
        HttpServletResponse mockedHttpServletResponse = mock(HttpServletResponse.class);

        cookieService.deleteCookieByName("testName", mockedHttpServletResponse);

        verify(mockedHttpServletResponse).addCookie(argThat(cookie ->
                cookie.getName().equals("testName") &&
                        cookie.getMaxAge() == 0 &&
                        cookie.getValue() == null &&
                        cookie.isHttpOnly() &&
                        cookie.getSecure() &&
                        "/".equals(cookie.getPath())
        ));
    }
}
