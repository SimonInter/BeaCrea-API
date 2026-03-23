package fr.beacrea.filter;

import fr.beacrea.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.security.Principal;

/**
 * Filtre JAX-RS qui s'exécute sur toutes les requêtes.
 * Si un header "Authorization: Bearer <token>" est présent et valide,
 * il injecte le SecurityContext avec les infos de l'utilisateur.
 * Si le token est absent, la requête passe (routes publiques autorisées).
 * Si le token est présent mais invalide, retourne 401.
 */
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {

    @Inject
    JwtService mJwtService;

    @Override
    public void filter(ContainerRequestContext pContext) {
        String lAuthHeader = pContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (lAuthHeader == null || !lAuthHeader.startsWith("Bearer ")) {
            return; // Route publique — pas de token requis ici
        }

        String lToken = lAuthHeader.substring(7);
        try {
            Claims lClaims = mJwtService.validateToken(lToken);
            String lUserId = mJwtService.extractUserId(lClaims);
            String lRole = mJwtService.extractRole(lClaims);
            pContext.setSecurityContext(new JwtSecurityContext(lUserId, lRole));
        } catch (JwtException lException) {
            pContext.abortWith(
                jakarta.ws.rs.core.Response.status(401)
                    .entity("{\"error\":\"Token invalide ou expiré\"}")
                    .type("application/json")
                    .build()
            );
        }
    }

    /**
     * Implémentation de SecurityContext portant les infos du JWT.
     */
    private record JwtSecurityContext(String userId, String role) implements SecurityContext {

        @Override
        public Principal getUserPrincipal() {
            return () -> userId;
        }

        @Override
        public boolean isUserInRole(String pRole) {
            return role != null && role.equals(pRole);
        }

        @Override
        public boolean isSecure() {
            return true;
        }

        @Override
        public String getAuthenticationScheme() {
            return "Bearer";
        }
    }
}
