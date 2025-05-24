package com.marketplace.user.service.validator;

import com.marketplace.usercore.service.UserServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
public class EmailValidator {

    private final UserServiceFacade userServiceFacade;

    public void validateEmailUniqueness(String newEmail, String currentEmail, Errors errors) {
        if (newEmail == null || newEmail.equals(currentEmail)) return;

        if (userServiceFacade.existsByEmail(newEmail)) {
            errors.rejectValue("email", "error.email", "User with this email already exists");
        }
    }
}
