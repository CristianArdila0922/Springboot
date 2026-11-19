package com.logitrack.audit;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Serializador JSON de apoyo para construir los snapshots de auditoria.
 * Registra el modulo de fechas de Java 8 (LocalDateTime) y es tolerante a
 * beans sin propiedades serializables.
 */
public final class AuditJsonSerializer {

    private static final JsonMapper MAPPER = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .build();

    private AuditJsonSerializer() {
    }

    public static String toJson(Object entity) {
        if (entity == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(entity);
        } catch (Exception e) {
            return "[error al serializar: " + e.getMessage() + "]";
        }
    }
}
