package com.gamesUP.gamesUP.security;

import org.springframework.stereotype.Component;

/**
 * Implémentation temporaire, sans hachage réel : Spring Security n'est pas encore en place (voir
 * étape 3). À remplacer par une implémentation basée sur {@code BCryptPasswordEncoder} dès que la
 * dépendance spring-boot-starter-security sera activée.
 */
@Component
public class NoOpPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String rawPassword) {
        return rawPassword;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword.equals(hashedPassword);
    }
}
