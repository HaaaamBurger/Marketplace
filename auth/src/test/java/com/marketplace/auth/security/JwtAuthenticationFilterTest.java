package com.marketplace.auth.security;

import com.marketplace.auth.config.AuthApplicationConfig;
import com.marketplace.auth.security.cookie.CookieNotFoundException;
import com.marketplace.auth.security.cookie.CookieService;
import com.marketplace.auth.security.token.JwtService;
import com.marketplace.auth.security.token.TokenPayload;
import com.marketplace.auth.service.JwtCookieManager;
import com.marketplace.auth.service.JwtTokenManager;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import io.jsonwebtoken.JwtException;
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
import static com.marketplace.auth.security.cookie.CookieService.COOKIE_REFRESH_TOKEN;
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
    private SecurityContextHolder securityContextHolder;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private FilterChain filterChain;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    public void doFilterInternal_ShouldSuccessfullyValidateToken() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
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
            verify(mockedCookie).getValue();
            verify(jwtTokenManager).getUserDetailsIfTokenValidOrThrow(accessToken);

            verify(mockedSecurityContext).setAuthentication(authenticationArgumentCaptor.capture());
            assertThat(authenticationArgumentCaptor.getValue()).isNotNull();
            assertThat(authenticationArgumentCaptor.getValue().isAuthenticated()).isTrue();

            verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        }
    }

    @Test
    public void doFilterInternal_ShouldSuccessfullyValidateToken_WhenContextAlreadyExists() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            Authentication mockedAuthentication = mock(Authentication.class);
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);

            when(mockedSecurityContext.getAuthentication()).thenReturn(mockedAuthentication);

            jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            verify(mockedSecurityContext).getAuthentication();
            verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        }
    }

    @Test
    public void doFilterInternal_ShouldRedirectToSignInPage_WhenTokenInvalidExceptionAndRefreshNotValid() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);
            Cookie mockedCookie = mock(Cookie.class);
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);

            when(mockedSecurityContext.getAuthentication()).thenReturn(null);
            when(cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest)).thenReturn(mockedCookie);
            when(mockedCookie.getValue()).thenReturn(accessToken);
            when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(accessToken)).thenThrow(JwtException.class);
            when(cookieService.extractCookieByName(COOKIE_REFRESH_TOKEN, httpServletRequest)).thenReturn(mockedCookie);
            when(mockedCookie.getValue()).thenReturn(refreshToken);
            when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(refreshToken)).thenThrow(JwtException.class);

            jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            verify(mockedSecurityContext).getAuthentication();
            verify(cookieService).extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest);
            verify(cookieService).extractCookieByName(COOKIE_REFRESH_TOKEN, httpServletRequest);
            verify(mockedCookie, times(2)).getValue();
            verify(jwtTokenManager, times(2)).getUserDetailsIfTokenValidOrThrow(refreshToken);
            verify(httpServletResponse).sendRedirect("/sign-in");
        }
    }

    @Test
    public void doFilterInternal_ShouldSuccessfullyValidateToken_WhenTokenInvalidExceptionAndRefreshIsValid() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);
            Cookie mockedAccessCookie = mock(Cookie.class);
            Cookie mockedRefreshCookie = mock(Cookie.class);
            String accessToken = "accessToken";
            String refreshToken = "refreshToken";
            User mockedUser = mock(User.class);
            TokenPayload mockedTokenPayload = mock(TokenPayload.class);

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);

            when(mockedSecurityContext.getAuthentication()).thenReturn(null);
            when(cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest)).thenReturn(mockedAccessCookie);
            when(mockedAccessCookie.getValue()).thenReturn(accessToken);
            when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(accessToken)).thenThrow(JwtException.class);
            when(cookieService.extractCookieByName(COOKIE_REFRESH_TOKEN, httpServletRequest)).thenReturn(mockedRefreshCookie);
            when(mockedRefreshCookie.getValue()).thenReturn(refreshToken);
            when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(refreshToken)).thenReturn(mockedUser);
            when(jwtTokenManager.generateTokenPayload(mockedUser)).thenReturn(mockedTokenPayload);

            jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            verify(mockedSecurityContext).getAuthentication();
            verify(cookieService).extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest);
            verify(mockedAccessCookie).getValue();
            verify(jwtTokenManager).getUserDetailsIfTokenValidOrThrow(accessToken);
            verify(cookieService).extractCookieByName(COOKIE_REFRESH_TOKEN, httpServletRequest);
            verify(mockedRefreshCookie).getValue();
            verify(jwtTokenManager).getUserDetailsIfTokenValidOrThrow(refreshToken);
            verify(jwtTokenManager).generateTokenPayload(mockedUser);

            verify(httpServletResponse, never()).sendRedirect("/sign-in");
        }
    }

    @Test
    public void doFilterInternal_ShouldRedirectToErrorPage_WhenUserBlocked() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);
            Cookie mockedCookie = mock(Cookie.class);
            String accessToken = "accessToken";
            User mockedUser = mock(User.class);

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);

            when(mockedSecurityContext.getAuthentication()).thenReturn(null);
            when(cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest)).thenReturn(mockedCookie);
            when(mockedCookie.getValue()).thenReturn(accessToken);
            when(jwtTokenManager.getUserDetailsIfTokenValidOrThrow(accessToken)).thenReturn(mockedUser);
            when(mockedUser.getStatus()).thenReturn(UserStatus.BLOCKED);

            jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            verify(mockedSecurityContext).getAuthentication();
            verify(cookieService).extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest);
            verify(mockedCookie).getValue();
            verify(jwtTokenManager).getUserDetailsIfTokenValidOrThrow(accessToken);
            verify(jwtCookieManager).deleteTokensFromCookie(httpServletResponse);
        }
    }

    @Test
    public void doFilterInternal_ShouldSuccessfullyValidateToken_WhenNoCookie() throws ServletException, IOException {
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            SecurityContext mockedSecurityContext = mock(SecurityContext.class);

            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(mockedSecurityContext);

            when(mockedSecurityContext.getAuthentication()).thenReturn(null);
            when(cookieService.extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest)).thenThrow(CookieNotFoundException.class);

            jwtAuthenticationFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);

            verify(mockedSecurityContext).getAuthentication();
            verify(cookieService).extractCookieByName(COOKIE_ACCESS_TOKEN, httpServletRequest);
            verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
