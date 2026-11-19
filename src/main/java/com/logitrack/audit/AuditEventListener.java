package com.logitrack.audit;

import com.logitrack.model.Auditoria;
import com.logitrack.repository.AuditoriaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

/**
 * Recibe los {@link AuditEvent} publicados por el {@code AuditoriaListener}
 * una vez confirmada la transaccion original y los persiste como registros de
 * auditoria en una transaccion nueva e independiente.
 */
@Component
public class AuditEventListener {

    private final AuditoriaRepository auditoriaRepository;

    public AuditEventListener(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onAuditEvent(AuditEvent event) {
        Auditoria auditoria = new Auditoria();
        auditoria.setTipoOperacion(event.operacion());
        auditoria.setFechaHora(LocalDateTime.now());
        auditoria.setUsuario(event.usuario());
        auditoria.setEntidad(event.entidad());
        auditoria.setEntidadId(event.entidadId());
        auditoria.setValorAnterior(event.valorAnterior());
        auditoria.setValorNuevo(event.valorNuevo());
        auditoriaRepository.save(auditoria);
    }
}
