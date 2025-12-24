package com.example.zhukov.cloudbox.service;

import com.example.zhukov.cloudbox.config.AbstractIT;
import com.example.zhukov.cloudbox.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import static org.junit.Assert.assertThrows;

@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceIT extends AbstractIT {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Создание user и сохранение в БД")
    public void shouldCreateUser() {
        userService.registerUser("zhukov", "zhukov@gmail.com", "123456");

        var user = userRepository.findByUsername("zhukov")
                .orElseThrow();

        Assertions.assertNotNull(user);
        Assertions.assertEquals("zhukov", user.getUsername());
    }

    @Test
    @DisplayName("Имя пользователя уже занято")
    public void shouldThrowExceptionWhenUsernameExists() {
        userService.registerUser("zhukov", "zhukov@gmail.com", "123456");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser("zhukov", "zhukov@gmail.com", "123456")
        );

        Assertions.assertEquals("Имя пользователя уже занято", exception.getMessage());
    }

}