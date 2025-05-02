package com.marketplace.user.web.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.common.util.EntityMapper;
import com.marketplace.user.web.dto.UserRequest;
import com.marketplace.user.web.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEntityMapper implements EntityMapper<User, UserRequest, UserResponse> {

    @Override
    public UserResponse mapEntityToResponseDto(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    public User mapRequestDtoToEntity(UserRequest userRequest) {
        return User.builder()
                .email(userRequest.getEmail())
                .role(userRequest.getRole())
                .password(userRequest.getPassword())
                .build();
    }

    public List<UserResponse> mapEntitiesToResponseDtos(List<User> users) {
        return users.stream()
                .map(this::mapEntityToResponseDto)
                .toList();
    }
}
