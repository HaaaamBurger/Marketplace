package com.marketplace.usercore.service;

import com.marketplace.usercore.model.User;

public interface UserSettingsService {

    void throwIfUserExistsByEmail(String email);

    User throwIfUserNotFoundById(String userId);

    void throwIfUserWithSameEmailExists(String email);

    boolean validateEntityOwnerOrAdmin(User authUser, String userId);

    boolean existsByEmail(String email);

}
