package com.marketplace.user.web.view;

import com.marketplace.user.service.validator.UserCreateValidator;
import com.marketplace.user.service.validator.UserUpdateValidator;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.mapper.UserEntityMapper;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.service.UserService;
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

    private final UserService userService;

    private final UserUpdateValidator userUpdateValidator;

    private final UserCreateValidator userCreateValidator;

    private final UserEntityMapper userEntityMapper;

    @GetMapping
    public String getAll(Model model) {
        List<UserResponse> userResponses = userEntityMapper.mapUsersToUserResponseDtos(userService.findAll());
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

        userService.create(userRequest);

        return "redirect:/users";
    }

    @GetMapping("/update/{userId}")
    public String getUpdateUser(Model model, @PathVariable String userId) {
        User user = userService.findById(userId);

        UserUpdateRequest userUpdateRequest = userEntityMapper.mapUserEntityToUserUpdateRequestDto(user);
        model.addAttribute("userUpdateRequest", userUpdateRequest);

        return "user-update";
    }

    @PutMapping("/update/{userId}")
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
        userService.update(userId, userUpdateRequest);

        return "redirect:/users";
    }

    @DeleteMapping("/{userId}")
    public String deleteUser(@PathVariable String userId) {
        userService.delete(userId);
        return "redirect:/users";
    }
}
