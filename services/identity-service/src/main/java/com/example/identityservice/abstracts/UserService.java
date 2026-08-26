package com.example.identityservice.abstracts;

import org.springframework.security.core.Authentication;

import com.example.identityservice.dtos.UpdateUser;
import com.example.identityservice.models.Users;


public interface UserService {
    Users updateUser(Authentication authentication, UpdateUser updatedUser);
    Users getUserByAuth(Authentication authentication);
}
