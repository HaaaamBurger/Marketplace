package com.marketplace.user.service.validator;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.MongoUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class UserUpdateValidator implements Validator {

    private final MongoUserService mongoUserService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserUpdateRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserUpdateRequest userUpdateRequest = (UserUpdateRequest) target;

        validateUserEmailExistence(userUpdateRequest.getEmail(), errors);
    }


    private void validateUserEmailExistence(String email, Errors errors) {
        try {
            mongoUserService.throwIfUserWithSameEmailExists(email);
        } catch (EntityExistsException exception) {
            errors.rejectValue(
                    "email",
                    "error.email",
                    "User with this email already exists");
        }
    }
}
