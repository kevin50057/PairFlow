package com.pairflow.config;

import com.pairflow.common.error.ApiException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Convenience accessor for the authenticated principal in controllers/services. */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static AuthUser get() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthUser u) {
            return u;
        }
        throw ApiException.unauthorized("Not authenticated");
    }

    public static String id() {
        return get().id();
    }
}
