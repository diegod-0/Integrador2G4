package org.rescuelink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record LoginRequest(
            @NotBlank(message = "El email es obligatorio")
            @Email(message = "El formato de email es inválido")
            String email,

            @NotBlank(message = "La contraseña es obligatoria")
            @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
            String password
    ) {}

    public record AuthResponse(
            String accessToken,
            long expiresIn,
            String email,
            String rol,
            String nombre
    ) {}

    public record TokenRefreshResponse(
            String accessToken,
            long expiresIn
    ) {}
}
