package com.logitrack.service;

import com.logitrack.dto.ProductoMasMovidoDTO;
import com.logitrack.dto.ReporteGeneralDTO;
import com.logitrack.dto.StockPorBodegaDTO;
import com.logitrack.repository.DetalleMovimientoRepository;
import com.logitrack.repository.InventarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Genera los reportes de resumen general: stock total por bodega y productos
 * mas movidos.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteService {

    private final InventarioRepository inventarioRepository;
    private final DetalleMovimientoRepository detalleMovimientoRepository;

    public List<StockPorBodegaDTO> stockTotalPorBodega() {
        return inventarioRepository.findStockTotalPorBodega().stream()
                .map(p -> new StockPorBodegaDTO(p.getBodegaId(), p.getBodegaNombre(), p.getTotal()))
                .toList();
    }

    public List<ProductoMasMovidoDTO> productosMasMovidos() {
        return detalleMovimientoRepository.findProductosMasMovidos().stream()
                .map(p -> new ProductoMasMovidoDTO(p.getProductoId(), p.getProductoNombre(), p.getTotal()))
                .toList();
    }

    public ReporteGeneralDTO reporteGeneral() {
        return new ReporteGeneralDTO(LocalDateTime.now(), stockTotalPorBodega(), productosMasMovidos());
    }
}
