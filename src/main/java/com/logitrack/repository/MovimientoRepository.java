package com.logitrack.repository;

import com.logitrack.model.Movimiento;
import com.logitrack.model.enums.TipoMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {

    /** Movimientos dentro de un rango de fechas (BETWEEN). */
    List<Movimiento> findByFechaBetweenOrderByFechaDesc(LocalDateTime inicio, LocalDateTime fin);

    List<Movimiento> findByTipoMovimientoOrderByFechaDesc(TipoMovimiento tipo);

    List<Movimiento> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}
