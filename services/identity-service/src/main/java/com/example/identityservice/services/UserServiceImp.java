package com.example.identityservice.services;

import com.example.identityservice.abstracts.UserService;
import com.example.identityservice.dtos.UpdateUser;
import com.example.identityservice.models.Users;
import com.example.identityservice.repository.UserRepository;
import com.example.identityservice.share.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepo;

    @Override
    public Users updateUser(Authentication authentication, UpdateUser updatedUser) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        Users user = userRepo.findByUsername(username).orElseThrow(
            () -> CustomException.resourceNotFound("User not found")
        );

        if(updatedUser.username() != null){
            user.setUsername(updatedUser.username());
        }

        if(updatedUser.email() != null) {
            user.setEmail(updatedUser.email());
        }

        return userRepo.save(user);
    }

    @Override
    public Users getUserByAuth(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        return userRepo.findByUsername(username).orElseThrow(
            () -> CustomException.resourceNotFound("User not found")
        );
    }
}
