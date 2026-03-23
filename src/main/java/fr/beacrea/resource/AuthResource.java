package fr.beacrea.resource;

import fr.beacrea.entity.AppUser;
import fr.beacrea.service.JwtService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.time.Instant;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    JwtService mJwtService;

    /**
     * Connexion : vérifie email + mot de passe, retourne un JWT.
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest pRequest) {
        if (pRequest == null || pRequest.email() == null || pRequest.password() == null) {
            return Response.status(400).entity(Map.of("error", "Email et mot de passe requis")).build();
        }

        AppUser lUser = AppUser.findByEmail(pRequest.email().toLowerCase().trim());
        if (lUser == null || !BCrypt.checkpw(pRequest.password(), lUser.password)) {
            Log.warnf("Failed login attempt for email: %s", pRequest.email());
            return Response.status(401).entity(Map.of("error", "Email ou mot de passe incorrect")).build();
        }

        String lToken = mJwtService.generateToken(lUser.id, lUser.role);
        Log.infof("User logged in. userId=%s, role=%s", lUser.id, lUser.role);

        return Response.ok(Map.of(
            "token", lToken,
            "user", safeUser(lUser)
        )).build();
    }

    /**
     * Inscription : crée un compte utilisateur avec mot de passe haché.
     */
    @POST
    @Path("/register")
    @Transactional
    public Response register(RegisterRequest pRequest) {
        if (pRequest == null || pRequest.email() == null || pRequest.password() == null
                || pRequest.firstName() == null || pRequest.lastName() == null) {
            return Response.status(400).entity(Map.of("error", "Tous les champs sont requis")).build();
        }
        if (pRequest.password().length() < 8) {
            return Response.status(400).entity(Map.of("error", "Le mot de passe doit contenir au moins 8 caractères")).build();
        }

        String lEmail = pRequest.email().toLowerCase().trim();
        if (AppUser.findByEmail(lEmail) != null) {
            return Response.status(409).entity(Map.of("error", "Un compte existe déjà avec cet email")).build();
        }

        AppUser lUser = new AppUser();
        lUser.id = "user_" + System.currentTimeMillis();
        lUser.email = lEmail;
        lUser.password = BCrypt.hashpw(pRequest.password(), BCrypt.gensalt(10));
        lUser.firstName = pRequest.firstName().trim();
        lUser.lastName = pRequest.lastName().trim();
        lUser.phone = pRequest.phone();
        lUser.role = "user";
        lUser.createdAt = Instant.now().toString();
        lUser.persist();

        String lToken = mJwtService.generateToken(lUser.id, lUser.role);
        Log.infof("New user registered. userId=%s, email=%s", lUser.id, lEmail);

        return Response.status(201).entity(Map.of(
            "token", lToken,
            "user", safeUser(lUser)
        )).build();
    }

    /** Retourne les infos utilisateur sans le mot de passe. */
    private Map<String, Object> safeUser(AppUser pUser) {
        return Map.of(
            "id", pUser.id,
            "email", pUser.email,
            "firstName", pUser.firstName != null ? pUser.firstName : "",
            "lastName", pUser.lastName != null ? pUser.lastName : "",
            "role", pUser.role
        );
    }

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String firstName, String lastName, String phone) {}
}
