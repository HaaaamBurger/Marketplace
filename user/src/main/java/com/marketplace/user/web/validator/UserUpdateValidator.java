package com.marketplace.user.web.validator;

import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserCrudService;
import com.marketplace.usercore.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
public class UserUpdateValidator {

    private final UserCrudService userCrudService;

    private final EmailValidator emailValidator;

    public void validateUserUpdateRequest(String userId, UserUpdateRequest userUpdateRequest, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        User user = userCrudService.findById(userId);
        emailValidator.validateEmailUniqueness(userUpdateRequest.getEmail(), user.getEmail(), errors);
    }

}
