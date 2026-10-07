package org.rescuelink.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;

    // Clave secreta HMAC-SHA256 de 256 bits para tests
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 900000L); // 15 minutos

        userDetails = new User(
                "diego.claros@rescuelink.org",
                "Password123!",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }

    @Test
    @DisplayName("Debe generar token JWT válido y no nulo")
    void generateToken_RetornaTokenValido() {
        String token = jwtService.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(3, token.split("\\.").length, "Un JWT debe constar de 3 segmentos (Header, Payload, Signature)");
    }

    @Test
    @DisplayName("Debe extraer el subject/username correcto desde el token JWT")
    void extractUsername_ExtraeEmailCorrecto() {
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        assertEquals("diego.claros@rescuelink.org", username);
    }

    @Test
    @DisplayName("Debe validar exitosamente el token para el usuario emisor")
    void isTokenValid_TokenValido_RetornaTrue() {
        String token = jwtService.generateToken(userDetails);
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("Debe rechazar el token si pertenece a otro usuario")
    void isTokenValid_UsuarioDiferente_RetornaFalse() {
        String token = jwtService.generateToken(userDetails);

        UserDetails otroUsuario = new User(
                "pedro.cueto@rescuelink.org",
                "Password123!",
                Collections.singletonList(new SimpleGrantedAuthority("VOLUNTARIO"))
        );

        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        assertFalse(isValid);
    }
}
