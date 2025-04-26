package com.marketplace.user.service;

import com.marketplace.auth.web.model.User;
import com.marketplace.common.model.UserStatus;
import com.marketplace.user.web.dto.UserCreateRequest;
import com.marketplace.user.web.dto.UserUpdateRequest;

import java.util.List;

public interface UserService {

    User create(UserCreateRequest userCreateRequest);

    List<User> findAll();

    User findById(String userId);

    User update(String userId, UserUpdateRequest userUpdateRequest);

    void updateStatus(String userId, UserStatus userStatus);

    void delete(String userId);

}
