package fr.beacrea.resource;

import fr.beacrea.entity.AppUser;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    /**
     * Profil de l'utilisateur connecté (token JWT requis).
     */
    @GET
    @Path("/me")
    public Response getMe(@Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        String lUserId = pSecurityContext.getUserPrincipal().getName();
        AppUser lUser = AppUser.findById(lUserId);
        if (lUser == null) {
            return Response.status(404).entity(Map.of("error", "Utilisateur introuvable")).build();
        }
        return Response.ok(safeUser(lUser)).build();
    }

    /**
     * Mise à jour du profil de l'utilisateur connecté (token JWT requis).
     */
    @PATCH
    @Path("/me")
    @Transactional
    public Response updateMe(@Context SecurityContext pSecurityContext, AppUser pPartial) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        String lUserId = pSecurityContext.getUserPrincipal().getName();
        AppUser lUser = AppUser.findById(lUserId);
        if (lUser == null) {
            return Response.status(404).entity(Map.of("error", "Utilisateur introuvable")).build();
        }
        if (pPartial.firstName != null) lUser.firstName = pPartial.firstName;
        if (pPartial.lastName != null) lUser.lastName = pPartial.lastName;
        if (pPartial.phone != null) lUser.phone = pPartial.phone;
        if (pPartial.addresses != null) lUser.addresses = pPartial.addresses;
        if (pPartial.wishlist != null) lUser.wishlist = pPartial.wishlist;
        return Response.ok(safeUser(lUser)).build();
    }

    // --- Routes compat JSON Server (utilisées par le frontend actuel) ---

    @GET
    public Response list(@QueryParam("email") String pEmail) {
        if (pEmail != null) {
            AppUser lUser = AppUser.findByEmail(pEmail);
            return Response.ok(lUser != null ? List.of(safeUser(lUser)) : List.of()).build();
        }
        return Response.ok(AppUser.listAll()).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String pId, AppUser pPartial) {
        AppUser lUser = AppUser.findById(pId);
        if (lUser == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (pPartial.firstName != null) lUser.firstName = pPartial.firstName;
        if (pPartial.lastName != null) lUser.lastName = pPartial.lastName;
        if (pPartial.phone != null) lUser.phone = pPartial.phone;
        if (pPartial.addresses != null) lUser.addresses = pPartial.addresses;
        if (pPartial.wishlist != null) lUser.wishlist = pPartial.wishlist;
        return Response.ok(safeUser(lUser)).build();
    }

    private Map<String, Object> safeUser(AppUser pUser) {
        return Map.of(
            "id", pUser.id,
            "email", pUser.email,
            "firstName", pUser.firstName != null ? pUser.firstName : "",
            "lastName", pUser.lastName != null ? pUser.lastName : "",
            "phone", pUser.phone != null ? pUser.phone : "",
            "role", pUser.role,
            "addresses", pUser.addresses != null ? pUser.addresses : List.of(),
            "wishlist", pUser.wishlist != null ? pUser.wishlist : List.of()
        );
    }
}
