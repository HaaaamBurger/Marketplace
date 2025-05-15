package com.marketplace.usercore.mapper;

import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEntityMapper {

    public UserResponse mapUserToUserResponseDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public User mapUserRequestDtoToUser(UserRequest userRequest) {
        return User.builder()
                .email(userRequest.getEmail())
                .role(userRequest.getRole())
                .password(userRequest.getPassword())
                .build();
    }

    public UserUpdateRequest mapUserEntityToUserUpdateRequestDto(User user) {
        return UserUpdateRequest.builder()
                .email(user.getEmail())
                .status(user.getStatus())
                .role(user.getRole())
                .build();
    }

    public List<UserResponse> mapUsersToUserResponseDtos(List<User> users) {
        return users.stream()
                .map(this::mapUserToUserResponseDto)
                .toList();
    }
}
