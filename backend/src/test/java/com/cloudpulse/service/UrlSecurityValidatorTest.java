package com.cloudpulse.service;

import com.cloudpulse.exception.SecurityValidationException;
import com.cloudpulse.security.UrlSecurityValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("URL Security Validator - SSRF Protection Unit Tests")
class UrlSecurityValidatorTest {

    private UrlSecurityValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UrlSecurityValidator();
        validator.setAllowPrivateIps(false);
        validator.setAllowLocalhost(false);
    }

    @Test
    @DisplayName("Should permit valid public HTTPS endpoint")
    void testValidPublicHttpsUrl() {
        assertDoesNotThrow(() -> validator.validateUrl("https://api.github.com"));
        assertDoesNotThrow(() -> validator.validateUrl("https://httpbin.org/status/200"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://169.254.169.254/latest/meta-data/",
            "http://169.254.170.2/v2/metadata",
            "http://100.100.100.200/latest/meta-data/"
    })
    @DisplayName("Should block cloud instance metadata endpoints (AWS, GCP, Azure, Alibaba)")
    void testBlockCloudMetadataEndpoints(String url) {
        SecurityValidationException ex = assertThrows(SecurityValidationException.class,
                () -> validator.validateUrl(url));
        assertTrue(ex.getMessage().contains("SSRF Protection") || ex.getMessage().contains("prohibited"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "http://127.0.0.1:8080/metrics",
            "http://localhost:3000/api",
            "http://0.0.0.0:8000"
    })
    @DisplayName("Should block loopback addresses when allowLocalhost is false")
    void testBlockLoopbackEndpoints(String url) {
        assertThrows(SecurityValidationException.class, () -> validator.validateUrl(url));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "file:///etc/passwd",
            "ftp://ftp.server.internal/files",
            "gopher://gopher.floodgap.com",
            "ldap://ldap.internal:389"
    })
    @DisplayName("Should block non-HTTP/HTTPS schemes")
    void testBlockNonHttpSchemes(String url) {
        SecurityValidationException ex = assertThrows(SecurityValidationException.class,
                () -> validator.validateUrl(url));
        assertTrue(ex.getMessage().contains("Invalid protocol") || ex.getMessage().contains("Only HTTP and HTTPS"));
    }

    @Test
    @DisplayName("Should block URLs with embedded user credentials")
    void testBlockEmbeddedCredentials() {
        assertThrows(SecurityValidationException.class,
                () -> validator.validateUrl("https://admin:supersecret@api.example.com"));
    }

    @Test
    @DisplayName("Should permit localhost when allowLocalhost is explicitly enabled")
    void testPermitLocalhostWhenConfigured() {
        validator.setAllowLocalhost(true);
        assertDoesNotThrow(() -> validator.validateUrl("http://localhost:8080/health"));
    }
}
