package com.marketplace.auth.service.validator;

import com.marketplace.auth.web.dto.AuthRequest;
import com.marketplace.usercore.service.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class SignUpValidator implements Validator {

    private final EmailValidator emailValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return AuthRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AuthRequest authRequest = (AuthRequest) target;
        emailValidator.validateUserExistenceByEmail(authRequest.getEmail(), errors);
    }
}
