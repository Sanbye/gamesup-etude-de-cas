package com.gamesUP.gamesUP.security;

/**
 * Abstraction isolant UserService de la stratégie de hachage de mot de passe. L'implémentation
 * temporaire {@link NoOpPasswordHasher} sera remplacée par un hachage BCrypt à l'étape 3, une fois
 * Spring Security mis en place, sans modifier le code appelant (principe ouvert/fermé).
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
