package fr.beacrea.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@ApplicationScoped
public class JwtService {

    private static final String CLAIM_ROLE = "role";

    @ConfigProperty(name = "beacrea.jwt.secret")
    String mSecret;

    @ConfigProperty(name = "beacrea.jwt.expiration-days")
    int mExpirationDays;

    private SecretKey getSigningKey() {
        byte[] lKeyBytes = mSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(lKeyBytes);
    }

    /**
     * Génère un JWT signé contenant l'identifiant et le rôle de l'utilisateur.
     */
    public String generateToken(String pUserId, String pRole) {
        long lNow = System.currentTimeMillis();
        long lExpiry = lNow + (long) mExpirationDays * 24 * 60 * 60 * 1000;

        return Jwts.builder()
                .subject(pUserId)
                .claim(CLAIM_ROLE, pRole)
                .issuedAt(new Date(lNow))
                .expiration(new Date(lExpiry))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valide un JWT et retourne ses claims. Lance une JwtException si invalide ou expiré.
     */
    public Claims validateToken(String pToken) throws JwtException {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(pToken)
                .getPayload();
    }

    public String extractUserId(Claims pClaims) {
        return pClaims.getSubject();
    }

    public String extractRole(Claims pClaims) {
        return pClaims.get(CLAIM_ROLE, String.class);
    }
}
