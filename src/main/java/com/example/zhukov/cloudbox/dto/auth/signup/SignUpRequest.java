package com.example.zhukov.cloudbox.dto.auth.signup;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotBlank(message = "Требуется имя пользователя ")
    @Size(min = 3, max = 20, message = "Имя пользователя должно содержать от 6 до 20 символов")
    private String username;

    @NotBlank
    @Email(message = "Неправильный формат почты")
    private String email;

    @NotBlank
    @Size(min = 6, message = "Пароль должен содержать не менее 6 символов")
    private String password;
}
