package com.logitrack.audit;

import com.logitrack.config.SpringContextHolder;
import com.logitrack.model.BaseEntity;
import com.logitrack.model.enums.TipoOperacion;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Listener de JPA ({@code @EntityListeners}) que dispara la auditoria
 * automaticamente sobre las entidades que implementan {@link AuditableEntity}.
 *
 * - {@code @PostLoad}:   guarda un snapshot del estado cargado (valores anteriores).
 * - {@code @PostPersist}: audita INSERT (valor nuevo = entidad actual).
 * - {@code @PostUpdate}:  audita UPDATE (valor anterior = snapshot, nuevo = actual).
 * - {@code @PostRemove}:  audita DELETE (valor anterior = entidad eliminada).
 *
 * La escritura del registro de auditoria se difiere a un
 * {@code @TransactionalEventListener} para evitar flushes anidados.
 */
public class AuditoriaListener {

    @PostLoad
    void postLoad(Object entity) {
        if (entity instanceof AuditableEntity a) {
            a.setAuditSnapshot(AuditJsonSerializer.toJson(entity));
        }
    }

    @PostPersist
    void postPersist(Object entity) {
        if (entity instanceof AuditableEntity a) {
            publish(TipoOperacion.INSERT, a, null, AuditJsonSerializer.toJson(entity));
        }
    }

    @PostUpdate
    void postUpdate(Object entity) {
        if (entity instanceof AuditableEntity a) {
            publish(TipoOperacion.UPDATE, a, a.getAuditSnapshot(), AuditJsonSerializer.toJson(entity));
        }
    }

    @PostRemove
    void postRemove(Object entity) {
        if (entity instanceof AuditableEntity a) {
            publish(TipoOperacion.DELETE, a, AuditJsonSerializer.toJson(entity), null);
        }
    }

    private void publish(TipoOperacion operacion, AuditableEntity entity, String valorAnterior, String valorNuevo) {
        ApplicationEventPublisher publisher = SpringContextHolder.getPublisher();
        if (publisher == null) {
            // El contexto de Spring aun no esta listo (p. ej. inicializacion).
            return;
        }
        String id = (entity instanceof BaseEntity b && b.getId() != null) ? b.getId().toString() : null;
        publisher.publishEvent(new AuditEvent(
                operacion,
                entity.getEntityName(),
                id,
                AuditContext.getCurrentUser(),
                valorAnterior,
                valorNuevo
        ));
    }
}
