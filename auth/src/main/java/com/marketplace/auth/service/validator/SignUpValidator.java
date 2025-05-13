package com.marketplace.auth.service.validator;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.usercore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class SignUpValidator implements Validator {

    private final UserRepository userRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AuthRequest authRequest = (AuthRequest) target;
        validateUserExistence(authRequest, errors);
    }


    private void validateUserExistence(AuthRequest authRequest, Errors errors) {
        if (userRepository.existsByEmail(authRequest.getEmail())) {
            errors.rejectValue(
                    "email",
                    "error.email",
                    "This email already in use");
        }
    }
}
