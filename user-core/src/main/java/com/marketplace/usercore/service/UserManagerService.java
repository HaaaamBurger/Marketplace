package com.marketplace.usercore.service;

import com.marketplace.usercore.model.User;

public interface UserManagerService {

    void throwIfUserExistsByEmail(String email);

    User throwIfUserNotFoundByIdOrGet(String userId);

    boolean existsByEmail(String email);

}
