package com.marketplace.auth.security;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.service.JwtCookieManager;
import com.marketplace.auth.service.JwtTokenManager;
import com.marketplace.usercore.model.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;

import static com.marketplace.auth.security.cookie.CookieService.COOKIE_ACCESS_TOKEN;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = AuthApplicationConfig.class)
@AutoConfigureMockMvc
public class JwtAuthenticationFilterTest {

    @MockitoBean
    private CookieService cookieService;

    @MockitoBean
    private JwtCookieManager jwtCookieManager;

    @MockitoBean
    private JwtTokenManager jwtTokenManager;

    @MockitoBean
    private HttpServletRequest httpServletRequest;

    @MockitoBean
    private HttpServletResponse httpServletResponse;

    @MockitoBean
    private FilterChain filterChain;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void doFilterInternal_ShouldSuccessfullyValidateToken() throws ServletException, IOException {
        MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class);
        SecurityContext mockedSecurityContext = mock(SecurityContext.class);
        Cookie mockedCookie = mock(Cookie.class);
        String accessToken = "accessToken";
        User mockedUser = mock(User.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);
        when(mockedSecurityContext.getAuthentication()).thenReturn(null);
        when(cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest)).thenReturn(mockedCookie);
        when(mockedCookie.getValue()).thenReturn(accessToken);
        when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(accessToken)).thenReturn(mockedUser);

        jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

        ArgumentCaptor<Authentication> authenticationArgumentCaptor = ArgumentCaptor.forClass(Authentication.class);

        verify(mockedSecurityContext).getAuthentication();
        verify(cookieService).extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest);
        verify(jwtTokenManager).getUserDetailsIfTokenValidOrThrow(accessToken);

        verify(mockedSecurityContext).setAuthentication(authenticationArgumentCaptor.capture());
        assertThat(authenticationArgumentCaptor.getValue()).isNotNull();
        assertThat(authenticationArgumentCaptor.getValue().isAuthenticated()).isTrue();

        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);

        mockedSecurityContextHolder.close();
    }
}
