package com.example.identityservice.abstracts;

import com.example.identityservice.dtos.LoginUser;
import com.example.identityservice.dtos.SignupUser;
import com.example.identityservice.models.Users;

public interface AuthService {
    String login(LoginUser loginUser);
    Users signUp(SignupUser signupUser);
    void verifyEmail(String token);
    String refreshToken(String refreshToken);
    public void resendVerificationEmail(String email);
}
