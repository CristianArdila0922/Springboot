package com.logitrack.dto;

import com.logitrack.model.enums.TipoMovimiento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Datos para registrar un movimiento de inventario.
 *
 * Reglas por tipo:
 * - ENTRADA:       se exige bodegaDestino (origen opcional).
 * - SALIDA:        se exige bodegaOrigen (destino opcional).
 * - TRANSFERENCIA: se exigen bodegaOrigen y bodegaDestino.
 */
public record MovimientoRequest(
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        Long bodegaOrigenId,

        Long bodegaDestinoId,

        @NotNull(message = "El listado de detalles es obligatorio")
        @Size(min = 1, message = "El movimiento debe tener al menos un detalle")
        @Valid
        List<DetalleMovimientoRequest> detalles
) {
}
