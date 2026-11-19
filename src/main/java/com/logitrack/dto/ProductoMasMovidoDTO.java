package com.logitrack.dto;

/**
 * Producto mas movido segun el total de unidades movidas (reporte general).
 */
public record ProductoMasMovidoDTO(
        Long productoId,
        String productoNombre,
        Long total
) {
}
