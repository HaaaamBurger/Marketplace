package com.marketplace.usercore.mapper;

import com.marketplace.common.mapper.EntityMapper;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.model.User;
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
