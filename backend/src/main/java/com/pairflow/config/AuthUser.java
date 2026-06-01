package com.pairflow.config;

/** The authenticated principal placed in the SecurityContext by {@link JwtAuthFilter}. */
public record AuthUser(String id, String email) {
}
