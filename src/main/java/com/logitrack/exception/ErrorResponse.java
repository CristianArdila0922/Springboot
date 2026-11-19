package com.logitrack.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Estructura estandar de las respuestas de error en JSON.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int status;
    private String error;
    private String mensaje;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> errores;
}
