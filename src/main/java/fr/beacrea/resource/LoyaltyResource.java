package fr.beacrea.resource;

import fr.beacrea.entity.LoyaltyTransaction;
import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Path("/loyaltyTransactions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LoyaltyResource {

    @GET
    public Response list(@QueryParam("userId") String pUserId,
                         @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        List<LoyaltyTransaction> lTransactions = LoyaltyTransaction.findByUserId(pUserId);
        return Response.ok(lTransactions).build();
    }

    @POST
    @Transactional
    public Response create(LoyaltyTransaction pTransaction,
                           @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        pTransaction.createdAt = Instant.now().toString();
        pTransaction.persist();
        Log.infof("Loyalty transaction created. userId=%s, points=%d, type=%s",
                pTransaction.userId, pTransaction.points, pTransaction.type);
        return Response.status(201).entity(pTransaction).build();
    }
}
