package com.example.identityservice.models;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.identityservice.enums.UserRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Users implements UserDetails {
    @Id
    @GeneratedValue(generator = "UUID")
    @UuidGenerator
    private UUID id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "is_verified", columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private boolean isVerified;

    @Column(name = "verification_token", unique = true, length = 255)
    private String verificationToken;

    @Enumerated(EnumType.STRING) // for JPA to store the enum as a string in the database not as an ordinal number
    @Column(name = "role", nullable = false, length = 50)
    private UserRole role = UserRole.CUSTOMER;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
         return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
//        return null;
    }
}
