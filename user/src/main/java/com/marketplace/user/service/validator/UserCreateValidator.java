package com.marketplace.user.service.validator;

import com.marketplace.common.exception.EntityExistsException;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.service.UserServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class UserCreateValidator implements Validator {

    private final UserServiceFacade userServiceFacade;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserRequest.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UserRequest userRequest = (UserRequest) target;
        validateUserExistenceByEmail(userRequest.getEmail(), errors);
    }

    private void validateUserExistenceByEmail(String email, Errors errors) {
        try {
            userServiceFacade.throwIfUserExistsByEmail(email);
        } catch (EntityExistsException exception) {
            errors.rejectValue(
                    "email",
                    "error.email",
                    "User with this email already exists");
        }
    }

}
