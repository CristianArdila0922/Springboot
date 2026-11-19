package com.logitrack.audit;

import com.logitrack.model.enums.TipoOperacion;

/**
 * Evento publicado por el {@code AuditoriaListener} cuando una entidad
 * auditada se inserta, actualiza o elimina. Se procesa despues de que la
 * transaccion original confirma (AFTER_COMMIT) para no interferir con el
 * flush de JPA.
 */
public record AuditEvent(
        TipoOperacion operacion,
        String entidad,
        String entidadId,
        String usuario,
        String valorAnterior,
        String valorNuevo
) {
}
