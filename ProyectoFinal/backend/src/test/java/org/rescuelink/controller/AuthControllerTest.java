package org.rescuelink.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.rescuelink.dto.ApiResponse;
import org.rescuelink.dto.AuthDtos.AuthResponse;
import org.rescuelink.dto.AuthDtos.LoginRequest;
import org.rescuelink.model.Usuario;
import org.rescuelink.model.enums.RolUsuario;
import org.rescuelink.repository.UsuarioRepository;
import org.rescuelink.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("Debe autenticar exitosamente y retornar token JWT con datos del usuario")
    void login_CredencialesValidas_RetornaAuthResponse() {
        // Arrange
        String email = "diego.claros@rescuelink.org";
        String password = "Password123!";
        LoginRequest request = new LoginRequest(email, password);

        UserDetails userDetailsMock = new User(
                email,
                password,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        Usuario usuarioMock = Usuario.builder()
                .id(UUID.randomUUID())
                .nombre("Diego Claros")
                .email(email)
                .passwordHash("$2a$12$hash")
                .rol(RolUsuario.ROLE_ADMIN)
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(email, password));
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetailsMock);
        when(jwtService.generateToken(userDetailsMock)).thenReturn("header.payload.signature");
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(usuarioMock));

        // Act
        ResponseEntity<ApiResponse<AuthResponse>> response = authController.login(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().success());
        assertEquals("header.payload.signature", response.getBody().data().accessToken());
        assertEquals("Diego Claros", response.getBody().data().nombre());
        assertEquals("ROLE_ADMIN", response.getBody().data().rol());
    }

    @Test
    @DisplayName("Debe propagar BadCredentialsException si las credenciales son incorrectas")
    void login_CredencialesInvalidas_LanzaBadCredentialsException() {
        // Arrange
        LoginRequest request = new LoginRequest("usuario@invalido.com", "PasswordInvalida");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales incorrectas"));

        // Act & Assert
        assertThrows(
                BadCredentialsException.class,
                () -> authController.login(request)
        );

        verify(jwtService, never()).generateToken(any());
    }
}
