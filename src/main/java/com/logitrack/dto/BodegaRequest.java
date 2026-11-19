package com.logitrack.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para crear o actualizar una bodega.
 */
public record BodegaRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
        String nombre,

        @NotBlank(message = "La ubicacion es obligatoria")
        @Size(max = 200, message = "La ubicacion no puede exceder 200 caracteres")
        String ubicacion,

        @NotNull(message = "La capacidad es obligatoria")
        @Min(value = 1, message = "La capacidad debe ser mayor a 0")
        Integer capacidad,

        @Size(max = 100, message = "El encargado no puede exceder 100 caracteres")
        String encargado
) {
}
