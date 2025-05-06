package com.marketplace.user.service;

import com.marketplace.auth.web.model.User;
import com.marketplace.auth.web.model.UserStatus;
import com.marketplace.user.web.dto.UserRequest;

import java.util.List;

public interface UserService {

    User create(UserRequest userRequest);

    List<User> findAll();

    User findById(String userId);

    User update(String userId, UserRequest userRequest);

    void updateStatus(String userId, UserStatus userStatus);

    void delete(String userId);

}
