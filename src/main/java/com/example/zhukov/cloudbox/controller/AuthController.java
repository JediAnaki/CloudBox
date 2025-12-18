package com.example.zhukov.cloudbox.controller;

import com.example.zhukov.cloudbox.dto.auth.signin.SignInRequest;
import com.example.zhukov.cloudbox.dto.auth.signin.SignInResponse;
import com.example.zhukov.cloudbox.dto.auth.signup.SignUpRequest;
import com.example.zhukov.cloudbox.dto.auth.signup.SignUpResponse;
import com.example.zhukov.cloudbox.security.UserDetailsImpl;
import com.example.zhukov.cloudbox.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest signUpRequest) {
        var user = userService.registerUser(signUpRequest.getUsername(), signUpRequest.getEmail(), signUpRequest.getPassword());
        SignUpResponse signUpResponse = new SignUpResponse(user.getUsername());
        return ResponseEntity.status(201).body(signUpResponse);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest signInRequest, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(signInRequest.getUsername(), signInRequest.getPassword());
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.getSession(true);
        String username = ((UserDetailsImpl) authentication.getPrincipal()).getUsername();
        return ResponseEntity.ok(new SignInResponse(username));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {session.invalidate();}
        return ResponseEntity.noContent().build();
    }
}
