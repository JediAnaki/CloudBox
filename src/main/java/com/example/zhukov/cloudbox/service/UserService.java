package com.example.zhukov.cloudbox.service;

import com.example.zhukov.cloudbox.entity.User;
import com.example.zhukov.cloudbox.repository.RoleRepository;
import com.example.zhukov.cloudbox.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserService {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(BCryptPasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       RoleRepository roleRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User registerUser(String username, String email, String password) {

        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Имя пользователя уже занято");
        }

        var userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role ROLE_USER not found"));

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(userRole))
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
        return userRepository.save(newUser);
    }

}
