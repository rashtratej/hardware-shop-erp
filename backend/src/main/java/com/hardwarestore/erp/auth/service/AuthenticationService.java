
package com.hardwarestore.erp.auth.service;

import com.hardwarestore.erp.auth.entity.AppUser;
import com.hardwarestore.erp.auth.repository.AppUserRepository;
import com.hardwarestore.erp.auth.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final AppUserRepository appUserRepository;
    private final JwtService jwtService;

    public AuthenticationService(
            AuthenticationManager authenticationManager,
            AppUserRepository appUserRepository,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.appUserRepository = appUserRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResult login(String email, String password) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email.trim(),
                                password
                        )
                );

        AppUser user = appUserRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow();

        user.setLastLoginAt(Instant.now());

        String accessToken = jwtService.generateAccessToken(user);

        return new LoginResult(
                accessToken,
                user.getId().toString(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    public record LoginResult(
            String accessToken,
            String userId,
            String email,
            String displayName,
            String role
    ) {}
}
