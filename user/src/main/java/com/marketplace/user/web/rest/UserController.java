package com.marketplace.user.web.rest;

import com.marketplace.auth.web.model.User;
import com.marketplace.user.service.UserService;
import com.marketplace.user.web.dto.UserRequest;
import com.marketplace.user.web.dto.UserResponse;
import com.marketplace.user.web.dto.UserStatusRequest;
import com.marketplace.user.web.util.UserEntityMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private final UserEntityMapper userEntityMapper;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest userRequest) {
        User user = userService.create(userRequest);
        return ResponseEntity.ok(userEntityMapper.mapEntityToResponseDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(userEntityMapper.mapEntitiesToResponseDtos(users));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getById(@PathVariable String userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(userEntityMapper.mapEntityToResponseDto(user));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UserRequest userRequest
    ) {
        User user = userService.update(userId, userRequest);
        return ResponseEntity.ok(userEntityMapper.mapEntityToResponseDto(user));
    }

    @PutMapping("/status")
    public void updateUserStatus(@Valid @RequestBody UserStatusRequest userStatusRequest) {
        userService.updateStatus(userStatusRequest.getUserId(), userStatusRequest.getStatus());
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.delete(userId);
    }

}
