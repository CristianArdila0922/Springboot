package com.logitrack.controller;

import com.logitrack.dto.ProductoMasMovidoDTO;
import com.logitrack.dto.ReporteGeneralDTO;
import com.logitrack.dto.StockPorBodegaDTO;
import com.logitrack.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes", description = "Reportes y resumenes de inventario")
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/stock-bodega")
    @Operation(summary = "Stock total por bodega")
    public ResponseEntity<List<StockPorBodegaDTO>> stockPorBodega() {
        return ResponseEntity.ok(reporteService.stockTotalPorBodega());
    }

    @GetMapping("/productos-movidos")
    @Operation(summary = "Productos mas movidos (por total de unidades)")
    public ResponseEntity<List<ProductoMasMovidoDTO>> productosMasMovidos() {
        return ResponseEntity.ok(reporteService.productosMasMovidos());
    }

    @GetMapping("/general")
    @Operation(summary = "Reporte general (resumen en JSON)",
            description = "Combina stock total por bodega y productos mas movidos.")
    public ResponseEntity<ReporteGeneralDTO> reporteGeneral() {
        return ResponseEntity.ok(reporteService.reporteGeneral());
    }
}
