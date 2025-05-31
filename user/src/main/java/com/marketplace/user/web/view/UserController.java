package com.marketplace.user.web.view;

import com.marketplace.user.service.validator.UserCreateValidator;
import com.marketplace.user.service.validator.UserUpdateValidator;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.mapper.UserEntityMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserCrudService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCrudService userCrudService;

    private final UserUpdateValidator userUpdateValidator;

    private final UserCreateValidator userCreateValidator;

    private final UserEntityMapper userEntityMapper;

    @GetMapping("/all")
    public String getAllUsers(Model model) {
        List<UserResponse> userResponses = userEntityMapper.mapUsersToUserResponseDtos(userCrudService.findAll());
        model.addAttribute("users", userResponses);

        return "users";
    }

    @GetMapping("/create")
    public String getCreateUser(
            @ModelAttribute UserRequest userRequest,
            Model model
    ) {
        model.addAttribute("userRequest", UserRequest.builder().build());
        return "user-create";
    }

    @PostMapping("/create")
    public String createUser(
            @Valid @ModelAttribute UserRequest userRequest,
            BindingResult bindingResult
    ) {
        userCreateValidator.validate(userRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "user-create";
        }

        userCrudService.create(userRequest);

        return "redirect:/users/all";
    }

    @GetMapping("/{userId}/update")
    public String getUpdateUser(Model model, @PathVariable String userId) {
        User user = userCrudService.findById(userId);

        UserUpdateRequest userUpdateRequest = userEntityMapper.mapUserEntityToUserUpdateRequestDto(user);
        model.addAttribute("userUpdateRequest", userUpdateRequest);

        return "user-update";
    }

    @PutMapping("/{userId}/update")
    public String updateUser(
            @ModelAttribute UserUpdateRequest userUpdateRequest,
            @PathVariable String userId,
            BindingResult bindingResult,
            Model model
    ) {
        userUpdateValidator.validateUserUpdateRequest(userId, userUpdateRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "user-update";
        }

        model.addAttribute("userId", userId);
        userCrudService.update(userId, userUpdateRequest);

        return "redirect:/users/all";
    }

    @DeleteMapping("/{userId}/delete")
    public String deleteUser(@PathVariable String userId) {
        userCrudService.delete(userId);
        return "redirect:/users/all";
    }
}
