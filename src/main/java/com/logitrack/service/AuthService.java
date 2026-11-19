package com.logitrack.service;

import com.logitrack.dto.AuthResponse;
import com.logitrack.dto.LoginRequest;
import com.logitrack.dto.RegisterRequest;
import com.logitrack.exception.BadRequestException;
import com.logitrack.model.Usuario;
import com.logitrack.model.enums.Rol;
import com.logitrack.repository.UsuarioRepository;
import com.logitrack.security.CustomUserDetails;
import com.logitrack.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails.getUsername(), userDetails.getRol());
        return new AuthResponse(token, "Bearer", userDetails.getUsername(), userDetails.getRol(), userDetails.getId());
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new BadRequestException("El nombre de usuario ya existe");
        }
        if (request.email() != null && !request.email().isBlank()
                && usuarioRepository.existsByEmail(request.email())) {
            throw new BadRequestException("El email ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.username());
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setEmail(request.email());
        usuario.setNombreCompleto(request.nombreCompleto());
        usuario.setRol(request.rol() != null ? request.rol() : Rol.EMPLEADO);
        usuario.setEnabled(true);
        usuarioRepository.save(usuario);

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol().name());
        return new AuthResponse(token, "Bearer", usuario.getUsername(), usuario.getRol().name(), usuario.getId());
    }
}
