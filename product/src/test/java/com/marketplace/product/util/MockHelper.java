package com.marketplace.product.util;

import com.marketplace.auth.web.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Service
@RequiredArgsConstructor
public class MockHelper {

    public User mockAuthenticationAndSetContext() {
        User user = UserDataBuilder.buildUserWithAllFields().build();
        return mockAuthenticationAndSetContext(user);
    }

    public User mockAuthenticationAndSetContext(User user) {

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        Authentication mockAuthentication = mock(Authentication.class);

        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(user);

        SecurityContextHolder.setContext(mockSecurityContext);

        return user;
    }

}
