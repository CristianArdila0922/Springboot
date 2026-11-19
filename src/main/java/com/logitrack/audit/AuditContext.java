package com.logitrack.audit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Obtiene el nombre del usuario autenticado en el hilo actual para
 * asignarlo a los registros de auditoria.
 */
public final class AuditContext {

    private AuditContext() {
    }

    public static String getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "SYSTEM";
        }
        return auth.getName();
    }
}
