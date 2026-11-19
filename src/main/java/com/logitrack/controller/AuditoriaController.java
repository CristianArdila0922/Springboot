package com.logitrack.controller;

import com.logitrack.model.Auditoria;
import com.logitrack.model.enums.TipoOperacion;
import com.logitrack.service.AuditoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditorias")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Auditoria", description = "Consulta de registros de auditoria por usuario o tipo de operacion")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping
    @Operation(summary = "Consultar auditorias",
            description = "Filtra por usuario y/o tipo de operacion (INSERT, UPDATE, DELETE). Sin filtros devuelve todo.")
    public ResponseEntity<List<Auditoria>> consultar(
            @RequestParam(required = false) String usuario,
            @RequestParam(required = false) TipoOperacion tipo) {

        if (usuario != null && tipo != null) {
            return ResponseEntity.ok(auditoriaService.findByUsuarioAndTipo(usuario, tipo));
        }
        if (usuario != null) {
            return ResponseEntity.ok(auditoriaService.findByUsuario(usuario));
        }
        if (tipo != null) {
            return ResponseEntity.ok(auditoriaService.findByTipoOperacion(tipo));
        }
        return ResponseEntity.ok(auditoriaService.findAll());
    }
}
