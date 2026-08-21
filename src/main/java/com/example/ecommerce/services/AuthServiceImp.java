package com.example.ecommerce.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ecommerce.abstracts.AuthService;
import com.example.ecommerce.config.JwtHelper;
import com.example.ecommerce.dtos.LoginUser;
import com.example.ecommerce.dtos.SignupUser;
import com.example.ecommerce.enums.UserRole;
import com.example.ecommerce.models.Users;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.share.CustomException;

@Service
public class AuthServiceImp implements AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtHelper jwtHelper;

    @Autowired
    private EmailService emailService;

    @Override
    public String login(LoginUser loginUser) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginUser.username(),
                loginUser.password()
            )
        );

        Users user = userRepository.findByUsername(loginUser.username())
                                    .orElseThrow(() -> CustomException.badCredentials());
        
        if(!user.isVerified()) {
            throw CustomException.badRequest("Please verify your email first");
        }

        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("userId", user.getId());
        customClaims.put("role", user.getRole());
        
        String refreshToken = jwtHelper.generateToken(customClaims, user, 1000 * 60 * 60 * 24 * 7); // 7 days
        
        return refreshToken;
    }

    @Override
    @Transactional
    public Users signUp(SignupUser signupUser) {
        Optional<Users> existingByUsername = userRepository.findByUsername(signupUser.username());
        if(existingByUsername.isPresent() && existingByUsername.get().isVerified()) {
            throw CustomException.conflict("Username already exists");
        }

        Optional<Users> existingByEmail = userRepository.findByEmail(signupUser.email());
        if(existingByEmail.isPresent() && existingByEmail.get().isVerified()) {
            throw CustomException.conflict("Email already exists");
        }
        
        Users newUser = new Users();
        newUser.setUsername(signupUser.username());
        newUser.setEmail(signupUser.email());
        newUser.setPassword(passwordEncoder.encode(signupUser.password()));
        newUser.setRole(UserRole.CUSTOMER);
        
        String token = jwtHelper.generateToken(newUser, 1000 * 60 * 5); // 5 minutes
        newUser.setVerificationToken(token);

        emailService.verifyAccountCreationEmail(signupUser.email(), token);
        return userRepository.save(newUser);
    }

    @Override
    public void verifyEmail(String token) {
        if (jwtHelper.isTokenExpired(token)) {
            throw CustomException.badRequest("Token has expired");
        }

        String username = jwtHelper.extractUsername(token);
        Users user = userRepository.findByUsername(username)
                                    .orElseThrow(() -> CustomException.resourceNotFound("User not found"));
        user.setVerified(true);
        userRepository.save(user);
    }

    @Override
    public void resendVerificationEmail(String email) {
        Users user = userRepository.findByEmail(email)
                                    .orElseThrow(() -> CustomException.resourceNotFound("User not found"));
        if(user.isVerified()) {
            throw CustomException.badRequest("User is already verified");
        }

        String token = jwtHelper.generateToken(user, 1000 * 60 * 5); // 5 minutes
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.verifyAccountCreationEmail(email, token);
    }

    @Override
    public String refreshToken(String refreshToken) {
        if (jwtHelper.isTokenExpired(refreshToken)) {
            throw CustomException.badRequest("Token has expired");
        }

        String username = jwtHelper.extractUsername(refreshToken);
        Users user = userRepository.findByUsername(username)
                                    .orElseThrow(() -> CustomException.resourceNotFound("User not found"));
        
        Map<String, Object> customClaims = new HashMap<>();
        customClaims.put("userId", user.getId());
        customClaims.put("role", user.getRole());
        
        return jwtHelper.generateToken(customClaims, user, 1000 * 60 * 60 * 10); // 10 hours
    }
}
