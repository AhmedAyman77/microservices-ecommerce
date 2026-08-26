package com.example.identityservice.controllers;

import java.util.UUID;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.identityservice.abstracts.AuthService;
import com.example.identityservice.dtos.LoginUser;
import com.example.identityservice.dtos.SignupUser;
import com.example.identityservice.share.GlobalResponse;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<GlobalResponse<String>> signup(@Valid @RequestBody SignupUser user) {
        authService.signUp(user);

        return ResponseEntity.status(201)
        .body(
            new GlobalResponse<>("User registered successfully wait for the verification email to verify your account")
        );
    }

    @PostMapping("/login")
    public ResponseEntity<GlobalResponse<List<String>>> login(@Valid @RequestBody LoginUser user) {
        String refreshToken = authService.login(user);
        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new GlobalResponse<>(List.of(accessToken, refreshToken)));
    }

    @GetMapping("/verify-email")
    public ResponseEntity<GlobalResponse<String>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new GlobalResponse<>("Email verified successfully"));
    }

    @GetMapping("/resend-verification-email")
    public ResponseEntity<GlobalResponse<String>> resendVerificationEmail(@RequestParam String email) {
        authService.resendVerificationEmail(email);
        return ResponseEntity.ok(new GlobalResponse<>("Verification email sent successfully"));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<GlobalResponse<String>> refreshToken(
        @RequestBody String refreshToken
    ) {
        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new GlobalResponse<>(accessToken));
    }
}
