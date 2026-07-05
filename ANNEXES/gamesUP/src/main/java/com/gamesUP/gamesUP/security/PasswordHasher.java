package com.gamesUP.gamesUP.security;

/**
 * Abstraction isolant UserService de la stratégie de hachage de mot de passe. Implémentée par
 * {@link BCryptPasswordHasher} (principe ouvert/fermé : UserService ne dépend jamais de BCrypt directement).
 */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);
}
