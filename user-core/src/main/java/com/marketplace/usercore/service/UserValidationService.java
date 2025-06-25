package com.marketplace.usercore.service;

import com.marketplace.usercore.model.User;

public interface UserValidationService {

    boolean validateEntityOwnerOrAdmin(User authUser, String userId);

}
