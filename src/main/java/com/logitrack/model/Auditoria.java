package com.logitrack.model;

import com.logitrack.model.enums.TipoOperacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro de auditoria. Se llena automaticamente desde el
 * {@code AuditoriaListener} cada vez que se inserta, actualiza o elimina
 * una entidad auditada.
 */
@Entity
@Table(name = "auditorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String entidad;

    @Column(name = "entidad_id")
    private String entidadId;

    @Lob
    @Column(name = "valor_anterior", columnDefinition = "TEXT")
    private String valorAnterior;

    @Lob
    @Column(name = "valor_nuevo", columnDefinition = "TEXT")
    private String valorNuevo;
}
