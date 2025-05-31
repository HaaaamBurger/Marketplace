package com.marketplace.usercore.service.validator;

import com.marketplace.usercore.model.User;
import com.marketplace.usercore.repository.UserRepository;
import com.marketplace.usercore.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailValidator {

    private final UserSettingsService userSettingsService;

    private final UserRepository userRepository;

    public void validateUserExistenceByEmail(String email, Errors errors) {
        if (userRepository.existsByEmail(email)) {
            rejectEmailValue(errors);
        }
    }

    public void validateEmailUniqueness(String newEmail, String currentEmail, Errors errors) {
        if (newEmail == null || newEmail.equals(currentEmail)) return;

        if (userSettingsService.existsByEmail(newEmail)) {
            rejectEmailValue(errors);
        }
    }

    public Optional<User> findUserOrRejectByEmail(String email, Errors errors) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            rejectEmailValue(errors);
        }

        return user;
    }

    private void rejectEmailValue(Errors errors) {
        errors.rejectValue(
                "email",
                "error.email",
                "This email already in use"
        );
    }

}
