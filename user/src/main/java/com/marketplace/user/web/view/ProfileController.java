package com.marketplace.user.web.view;

import com.marketplace.user.web.validator.ProfileUpdateValidator;
import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.mapper.SimpleUserMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.security.AuthenticationUserService;
import com.marketplace.usercore.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final SimpleUserMapper simpleUserMapper;

    private final ProfileUpdateValidator profileUpdateValidator;

    private final ProfileService profileService;

    private final AuthenticationUserService authenticationUserService;

    @GetMapping
    public String getProfile() {
        return "profile";
    }

    @GetMapping("/update")
    public String getUpdateProfile(Model model) {
        User authUser = (User) model.getAttribute("authUser");

        ProfileUpdateRequest profileUpdateRequest = simpleUserMapper.mapUserToProfileUpdateRequestDto(authUser);
        model.addAttribute("profileUpdateRequest", profileUpdateRequest);

        return "profile-update";
    }

    @PutMapping("/update")
    public String updateProfile(
            @ModelAttribute ProfileUpdateRequest profileUpdateRequest,
            BindingResult bindingResult
    ) {
        User authenticatedUser = authenticationUserService.getAuthenticatedUser();
        profileUpdateValidator.validate(authenticatedUser.getId(), profileUpdateRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "profile-update";
        }

        profileService.update(authenticatedUser.getId(), profileUpdateRequest);

        return "redirect:/profile";
    }
}
