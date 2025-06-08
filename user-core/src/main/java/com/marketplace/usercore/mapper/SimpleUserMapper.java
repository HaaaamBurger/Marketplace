package com.marketplace.usercore.mapper;

import com.marketplace.usercore.dto.ProfileUpdateRequest;
import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserResponse;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;

import java.util.List;

public interface SimpleUserMapper {

    UserResponse mapUserToUserResponseDto(User user);

    User mapUserRequestDtoToUser(UserRequest userRequest);

    UserUpdateRequest mapUserToUserUpdateRequestDto(User user);

    ProfileUpdateRequest mapUserToProfileUpdateRequestDto(User user);

    List<UserResponse> mapUsersToUserResponseDtos(List<User> users);

}
