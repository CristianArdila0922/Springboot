package com.logitrack.dto;

/**
 * Resumen de stock total por bodega (reporte general).
 */
public record StockPorBodegaDTO(
        Long bodegaId,
        String bodegaNombre,
        Long total
) {
}
