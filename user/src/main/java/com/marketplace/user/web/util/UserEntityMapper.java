package com.marketplace.user.web.util;

import com.marketplace.auth.web.model.User;
import com.marketplace.common.util.EntityMapper;
import com.marketplace.user.web.dto.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserEntityMapper implements EntityMapper<User, UserResponse> {

    @Override
    public UserResponse mapEntityToDto(User entity) {
        return UserResponse.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public User mapDtoToEntity(UserResponse dto) {
        return User.builder()
                .id(dto.getId())
                .email(dto.getEmail())
                .role(dto.getRole())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public List<UserResponse> mapEntitiesToDtos(List<User> users) {
        return users.stream()
                .map(this::mapEntityToDto)
                .toList();
    }

    public List<User> mapDtosToEntities(List<UserResponse> dtos) {
        return dtos.stream()
                .map(this::mapDtoToEntity)
                .toList();
    }

}
