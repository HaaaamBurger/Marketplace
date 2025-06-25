package com.marketplace.usercore.service;

import com.marketplace.usercore.model.User;

public interface UserManagerService {

    void throwIfUserExistsByEmail(String email);

    User throwIfUserNotFoundById(String userId);

    void throwIfUserWithSameEmailExists(String email);

    boolean existsByEmail(String email);

}
