package com.logitrack.dto;

/**
 * Respuesta de autenticacion: contiene el token JWT y los datos del usuario.
 */
public record AuthResponse(
        String token,
        String type,
        String username,
        String rol,
        Long userId
) {
}
