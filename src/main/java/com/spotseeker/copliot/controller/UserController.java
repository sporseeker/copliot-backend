package com.spotseeker.copliot.controller;

import com.spotseeker.copliot.dto.UserUpdateDto;
import com.spotseeker.copliot.model.User;
import com.spotseeker.copliot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getProfile(Authentication authentication) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userService.getProfile(userId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDto dto) {
        Long userId = Long.parseLong(authentication.getName());
        User user = userService.updateProfile(userId, dto);
        return ResponseEntity.ok(user);
    }
}
