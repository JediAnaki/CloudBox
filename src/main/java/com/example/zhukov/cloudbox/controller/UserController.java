package com.example.zhukov.cloudbox.controller;

import com.example.zhukov.cloudbox.dto.user.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserResponse userResponse = new UserResponse(username);
        return ResponseEntity.ok(userResponse);
    }
}
