package com.marketplace.auth.web.validator;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;
import com.marketplace.usercore.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SignInValidator implements Validator {

    private final PasswordEncoder passwordEncoder;

    private final EmailValidator emailValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        AuthRequest authRequest = (AuthRequest) target;
        Optional<User> userOptional = emailValidator.findUserOrRejectByEmail(authRequest.getEmail(), errors);

        userOptional.ifPresent(user -> {
            if (validateUserBlocked(user, errors)) {
                return;
            }
            validatePasswordMatching(authRequest.getPassword(), user, errors);
        });
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

    private boolean validateUserBlocked(User user, Errors errors) {
        if (user.getStatus() == UserStatus.BLOCKED) {
            errors.reject(
                    "error.status",
                    "User is blocked");

            return true;
        }

        return false;
    }


}
