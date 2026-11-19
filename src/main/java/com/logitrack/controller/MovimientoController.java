package com.logitrack.controller;

import com.logitrack.dto.MovimientoRequest;
import com.logitrack.model.Movimiento;
import com.logitrack.model.enums.TipoMovimiento;
import com.logitrack.security.CustomUserDetails;
import com.logitrack.service.MovimientoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Movimientos", description = "Registro y consulta de movimientos de inventario")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @GetMapping
    @Operation(summary = "Listar todos los movimientos")
    public ResponseEntity<List<Movimiento>> findAll() {
        return ResponseEntity.ok(movimientoService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un movimiento por id")
    public ResponseEntity<Movimiento> findById(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.findById(id));
    }

    @GetMapping("/fecha")
    @Operation(summary = "Movimientos por rango de fechas (BETWEEN)")
    public ResponseEntity<List<Movimiento>> findByFechaBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(movimientoService.findByFechaBetween(inicio, fin));
    }

    @GetMapping("/tipo/{tipo}")
    @Operation(summary = "Movimientos por tipo (ENTRADA, SALIDA, TRANSFERENCIA)")
    public ResponseEntity<List<Movimiento>> findByTipo(@PathVariable TipoMovimiento tipo) {
        return ResponseEntity.ok(movimientoService.findByTipo(tipo));
    }

    @PostMapping
    @Operation(summary = "Registrar un movimiento",
            description = "Registra una ENTRADA, SALIDA o TRANSFERENCIA y ajusta el stock. El usuario responsable se toma del token JWT.")
    public ResponseEntity<Movimiento> registrar(@Valid @RequestBody MovimientoRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        Movimiento movimiento = movimientoService.registrar(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(movimiento);
    }
}
