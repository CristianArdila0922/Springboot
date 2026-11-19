package com.logitrack.dto;

import com.logitrack.model.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos para registrar un nuevo usuario. Si no se indica un rol, se asigna
 * EMPLEADO por defecto.
 */
public record RegisterRequest(
        @NotBlank(message = "El nombre de usuario es obligatorio")
        @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, max = 100, message = "La contraseña debe tener entre 4 y 100 caracteres")
        String password,

        @Email(message = "El email debe tener un formato valido")
        String email,

        @Size(max = 100, message = "El nombre completo no puede exceder 100 caracteres")
        String nombreCompleto,

        Rol rol
) {
}
