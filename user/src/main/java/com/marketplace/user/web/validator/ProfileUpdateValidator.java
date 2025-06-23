package com.marketplace.user.web.validator;

import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserCrudService;
import com.marketplace.usercore.validator.EmailValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
public class ProfileUpdateValidator {

    private final EmailValidator emailValidator;

    private final UserCrudService userCrudService;

    public void validate(String userId, ProfileUpdateRequest profileUpdateRequest, Errors errors) {

        if (errors.hasErrors()) {
            return;
        }

        User user = userCrudService.findById(userId);
        emailValidator.validateEmailUniqueness(profileUpdateRequest.getEmail(), user.getEmail(), errors);
    }

}
