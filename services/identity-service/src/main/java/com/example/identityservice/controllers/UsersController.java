package com.example.identityservice.controllers;

import java.util.UUID;

import com.example.identityservice.dtos.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.identityservice.abstracts.UserService;
import com.example.identityservice.dtos.UpdateUser;
import com.example.identityservice.models.Users;
import com.example.identityservice.share.GlobalResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UsersController {
    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<GlobalResponse<UserResponse>> getUser(Authentication authentication) {
        Users user = userService.getUserByAuth(authentication);
        UserResponse res = new UserResponse(user.getUsername(), user.getEmail(), user.getRole());

        return ResponseEntity.status(200).body(
            new GlobalResponse<UserResponse> (res)
        );
    }

    @PutMapping
    public ResponseEntity<GlobalResponse<UserResponse>> updateUser(
        Authentication authentication,
        @Valid @RequestBody UpdateUser updatedUser) {
        Users user = userService.updateUser(authentication, updatedUser);
        UserResponse res = new UserResponse(user.getUsername(), user.getEmail(), user.getRole());

        return ResponseEntity.status(200).body(
            new GlobalResponse<UserResponse> (res)
        );
    }
}
