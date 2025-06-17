package com.marketplace.user.service.validator;

import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.service.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class UserCreateValidator implements Validator {

    private final EmailValidator emailValidator;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        UserRequest userRequest = (UserRequest) target;
        emailValidator.validateUserExistenceByEmail(userRequest.getEmail(), errors);
    }

}
