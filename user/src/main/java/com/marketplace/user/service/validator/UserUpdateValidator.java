package com.marketplace.user.service.validator;

import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserServiceFacade;
import com.marketplace.usercore.service.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
public class UserUpdateValidator {

    private final UserServiceFacade userServiceFacade;

    private final EmailValidator emailValidator;

    public void validateUserUpdateRequest(String userId, UserUpdateRequest userUpdateRequest, Errors errors) {
        User user = userServiceFacade.findById(userId);
        emailValidator.validateEmailUniqueness(userUpdateRequest.getEmail(), user.getEmail(), errors);
    }

}
