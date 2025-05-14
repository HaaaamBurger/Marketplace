package com.marketplace.usercore.service;


import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;
import com.marketplace.usercore.model.UserStatus;

import java.util.List;

public interface UserService {

    User create(UserRequest userRequest);

    List<User> findAll();

    User findById(String userId);

    User update(String userId, UserUpdateRequest userUpdateRequest);

    void updateStatus(String userId, UserStatus userStatus);

    void delete(String userId);

    boolean validateEntityOwnerOrAdmin(User user, String ownerId);
}
