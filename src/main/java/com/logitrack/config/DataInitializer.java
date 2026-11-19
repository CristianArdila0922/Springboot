package com.logitrack.config;

import com.logitrack.model.Usuario;
import com.logitrack.model.enums.Rol;
import com.logitrack.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea los usuarios por defecto (con contrasena cifrada con BCrypt) la primera
 * vez que arranca la aplicacion. Las credenciales de prueba son:
 *
 *   - admin    / admin123     (rol ADMIN)
 *   - empleado / empleado123  (rol EMPLEADO)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        crearSiNoExiste("admin", "admin123", "admin@logitrack.com", "Administrador General", Rol.ADMIN);
        crearSiNoExiste("empleado", "empleado123", "empleado@logitrack.com", "Juan Perez", Rol.EMPLEADO);
    }

    private void crearSiNoExiste(String username, String password, String email, String nombreCompleto, Rol rol) {
        if (usuarioRepository.existsByUsername(username)) {
            return;
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setEmail(email);
        usuario.setNombreCompleto(nombreCompleto);
        usuario.setRol(rol);
        usuario.setEnabled(true);
        usuarioRepository.save(usuario);
        log.info("Usuario por defecto creado: {} (rol {})", username, rol);
    }
}
