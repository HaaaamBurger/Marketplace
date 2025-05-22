package com.marketplace.user.service.validator;

import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

@Service
@RequiredArgsConstructor
public class ProfileUpdateValidator {

    private final EmailValidator emailValidator;

    private final UserServiceFacade userServiceFacade;

    public void validate(String userId, ProfileUpdateRequest profileUpdateRequest, Errors errors) {
        User user = userServiceFacade.findById(userId);
        emailValidator.validateEmailUniqueness(profileUpdateRequest.getEmail(), user.getEmail(), errors);
    }

}
