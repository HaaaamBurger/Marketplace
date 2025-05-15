package com.marketplace.user.web.view;

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

    private final UserEntityMapper userEntityMapper;

    @GetMapping
    public String getAll(Model model) {
        List<UserResponse> userResponses = userEntityMapper.mapEntitiesToResponseDtos(userService.findAll());
        model.addAttribute("users", userResponses);
        model.addAttribute("userRequest", UserRequest.builder().build());

        return "users";
    }

    @GetMapping("/profile")
    public String getById() {
        return "user";
    }

    @PostMapping
    public String createUser(@Valid @ModelAttribute UserRequest userRequest) {
        userService.create(userRequest);
        return "redirect:/users";
    }

    @GetMapping("/update/{userId}")
    public String getUpdateUser(Model model, @PathVariable String userId) {
        User user = userService.findById(userId);
        model.addAttribute("userUpdateRequest", UserUpdateRequest.builder()
                        .email(user.getEmail())
                        .status(user.getStatus())
                        .role(user.getRole())
                .build());

        return "user-edit";
    }

    @PutMapping("/update/{userId}")
    public String updateUser(
            @Valid @ModelAttribute UserUpdateRequest userUpdateRequest,
            @PathVariable String userId,
            BindingResult bindingResult,
            Model model
    ) {
        userUpdateValidator.validate(userUpdateRequest, bindingResult);
        if (bindingResult.hasErrors()) {
            return "user-edit";
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
