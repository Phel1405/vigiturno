package com.javeriana.vigiturno.services;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GoogleTokenService {

    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    @Value("${google.client-id:}")
    private String googleClientId;

    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleUserInfo verifyIdToken(String idToken) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new IllegalStateException("GOOGLE_CLIENT_ID no está configurado en el backend.");
        }

        if (idToken == null || idToken.isBlank()) {
            throw new IllegalArgumentException("El token de Google es obligatorio.");
        }

        String url = UriComponentsBuilder
                .fromHttpUrl(GOOGLE_TOKEN_INFO_URL)
                .queryParam("id_token", idToken)
                .toUriString();

        Map<?, ?> payload;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            payload = response.getBody();
        } catch (RestClientException ex) {
            throw new IllegalArgumentException("Google no pudo validar el token enviado.", ex);
        }

        if (payload == null) {
            throw new IllegalArgumentException("Google devolvió una respuesta vacía al validar el token.");
        }

        String audience = readString(payload, "aud");
        if (!googleClientId.equals(audience)) {
            throw new IllegalArgumentException("El token de Google no pertenece al Client ID configurado.");
        }

        String issuer = readString(payload, "iss");
        if (!"https://accounts.google.com".equals(issuer) && !"accounts.google.com".equals(issuer)) {
            throw new IllegalArgumentException("El emisor del token de Google no es válido.");
        }

        boolean emailVerified = Boolean.parseBoolean(readString(payload, "email_verified"));
        String email = readString(payload, "email");
        if (!emailVerified || email == null || email.isBlank()) {
            throw new IllegalArgumentException("Google no confirmó un correo verificado para este usuario.");
        }

        return new GoogleUserInfo(
                email,
                readString(payload, "name"),
                readString(payload, "picture"),
                readString(payload, "sub")
        );
    }

    private String readString(Map<?, ?> payload, String key) {
        Object value = payload.get(key);
        return value != null ? value.toString() : null;
    }

    public record GoogleUserInfo(String email, String name, String pictureUrl, String providerId) {}
}
