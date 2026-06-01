package com.pairflow.user;

import com.pairflow.config.CurrentUser;
import com.pairflow.user.dto.UpdateUserRequest;
import com.pairflow.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(userService.getById(CurrentUser.id()));
    }

    @PatchMapping("/me")
    public UserResponse updateMe(@Valid @RequestBody UpdateUserRequest req) {
        return UserResponse.from(userService.update(CurrentUser.id(), req));
    }
}
