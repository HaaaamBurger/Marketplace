package com.marketplace.auth.service.validator;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignInValidator implements Validator {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AuthRequest authRequest = (AuthRequest) target;

        Optional<User> userOptional = findUserOrRejectByEmail(authRequest.getEmail(), errors);
        userOptional.ifPresent(user -> validatePasswordMatching(authRequest.getPassword(), user, errors));
    }

    private Optional<User> findUserOrRejectByEmail(String email, Errors errors) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            errors.rejectValue(
                    "email",
                    "error.email",
                    "Email not found");
        }

        return user;
    }

    private void validatePasswordMatching(String rawPassword, User user, Errors errors) {
        boolean matches = passwordEncoder.matches(rawPassword, user.getPassword());

        if (!matches) {
            errors.rejectValue(
                    "password",
                    "error.password",
                    "Passwords not matching");
        }
    }

}
