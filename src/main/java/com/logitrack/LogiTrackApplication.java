package com.logitrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicacion LogiTrack.
 *
 * LogiTrack es un sistema de gestion y auditoria de bodegas construido con
 * Spring Boot, Spring Data JPA, Spring Security + JWT y MySQL.
 */
@SpringBootApplication
public class LogiTrackApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogiTrackApplication.class, args);
    }
}
