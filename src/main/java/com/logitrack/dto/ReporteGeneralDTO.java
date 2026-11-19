package com.logitrack.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Reporte de resumen general: stock total por bodega y productos mas movidos.
 */
public record ReporteGeneralDTO(
        LocalDateTime generadoEn,
        List<StockPorBodegaDTO> stockPorBodega,
        List<ProductoMasMovidoDTO> productosMasMovidos
) {
}
