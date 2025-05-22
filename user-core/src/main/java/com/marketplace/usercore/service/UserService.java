package com.marketplace.usercore.service;


import com.marketplace.usercore.dto.UserRequest;
import com.marketplace.usercore.dto.UserUpdateRequest;
import com.marketplace.usercore.model.User;

import java.util.List;

public interface UserService {

    User create(UserRequest userRequest);

    List<User> findAll();

    User findById(String userId);

    User update(String userId, UserUpdateRequest userUpdateRequest);

    void delete(String userId);

    boolean validateEntityOwnerOrAdmin(User authUser, String userId);
}
