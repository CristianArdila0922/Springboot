package com.logitrack.controller;

import com.logitrack.dto.BodegaRequest;
import com.logitrack.model.Bodega;
import com.logitrack.service.BodegaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bodegas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bodegas", description = "CRUD de bodegas (almacenes)")
public class BodegaController {

    private final BodegaService bodegaService;

    @GetMapping
    @Operation(summary = "Listar todas las bodegas")
    public ResponseEntity<List<Bodega>> findAll() {
        return ResponseEntity.ok(bodegaService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una bodega por id")
    public ResponseEntity<Bodega> findById(@PathVariable Long id) {
        return ResponseEntity.ok(bodegaService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Crear una bodega")
    public ResponseEntity<Bodega> create(@Valid @RequestBody BodegaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bodegaService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una bodega")
    public ResponseEntity<Bodega> update(@PathVariable Long id, @Valid @RequestBody BodegaRequest request) {
        return ResponseEntity.ok(bodegaService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar una bodega (solo ADMIN)")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        bodegaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
