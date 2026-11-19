package com.logitrack.audit;

/**
 * Marcador para entidades que deben ser auditadas automaticamente.
 *
 * La entidad expone un nombre logico, su identificador y un snapshot
 * (en formato JSON) de su estado que el {@code AuditoriaListener} utiliza
 * para registrar los valores anteriores/nuevos en la tabla de auditoria.
 */
public interface AuditableEntity {

    /** Nombre logico de la entidad (p. ej. "Bodega"). */
    String getEntityName();

    /** Snapshot JSON del estado cargado desde la base (valores anteriores). */
    String getAuditSnapshot();

    /** Guarda el snapshot JSON tomado en {@code @PostLoad}. */
    void setAuditSnapshot(String snapshot);
}
