package org.rescuelink.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.dto.AuthDtos.AuthResponse;
import org.rescuelink.dto.AuthDtos.LoginRequest;
import org.rescuelink.model.Usuario;
import org.rescuelink.repository.UsuarioRepository;
import org.rescuelink.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de inicio de sesión y gestión de credenciales JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión y obtener Bearer Access Token (15 min)")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(userDetails);
        Usuario usuario = usuarioRepository.findByEmail(request.email()).orElseThrow();

        AuthResponse authResponse = new AuthResponse(
                token,
                900, // 15 minutos en segundos
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre()
        );

        return ResponseEntity.ok(ApiResponse.ok("Sesión iniciada correctamente", authResponse));
    }
}
