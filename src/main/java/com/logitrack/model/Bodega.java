package com.logitrack.model;

import com.logitrack.audit.AuditableEntity;
import com.logitrack.audit.AuditoriaListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bodega (almacen). Cada bodega tiene una ubicacion, una capacidad maxima
 * y un encargado responsable.
 */
@Entity
@Table(name = "bodegas")
@EntityListeners(AuditoriaListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bodega extends BaseEntity implements AuditableEntity {

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(nullable = false)
    private String ubicacion;

    @Column(nullable = false)
    private Integer capacidad;

    private String encargado;

    @Transient
    private String auditSnapshot;

    @Override
    public String getEntityName() {
        return "Bodega";
    }
}
