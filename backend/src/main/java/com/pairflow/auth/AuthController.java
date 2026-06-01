package com.pairflow.auth;

import com.pairflow.auth.dto.AuthResponse;
import com.pairflow.auth.dto.LoginRequest;
import com.pairflow.auth.dto.RegisterRequest;
import com.pairflow.config.CurrentUser;
import com.pairflow.user.UserService;
import com.pairflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout() {
        // Stateless JWT: logout is client-side (drop the token). Endpoint exists for symmetry.
        return Map.of("ok", true);
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.getById(CurrentUser.id()));
    }
}
