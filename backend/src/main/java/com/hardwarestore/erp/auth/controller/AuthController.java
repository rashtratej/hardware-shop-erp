
package com.hardwarestore.erp.auth.controller;

import com.hardwarestore.erp.auth.dto.LoginRequest;
import com.hardwarestore.erp.auth.dto.LoginResponse;
import com.hardwarestore.erp.auth.security.AuthenticationService;
import com.hardwarestore.erp.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final long accessTokenExpiration;

    public AuthController(
            AuthenticationService authenticationService,
            @Value("${app.jwt.access-token-expiration}") long accessTokenExpiration
    ) {
        this.authenticationService = authenticationService;
        this.accessTokenExpiration = accessTokenExpiration;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {

        AuthenticationService.LoginResult result =
                authenticationService.login(
                        request.email(),
                        request.password()
                );

        LoginResponse response = new LoginResponse(
                result.accessToken(),
                "Bearer",
                accessTokenExpiration,
                result.userId(),
                result.email(),
                result.displayName(),
                result.role()
        );

        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response)
        );
    }
}
