package fr.beacrea.resource;

import fr.beacrea.entity.PromoCode;
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

@Path("/promoCodes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PromoCodeResource {

    @GET
    public Response list(@QueryParam("code") String pCode,
                         @QueryParam("active") Boolean pActive,
                         @Context SecurityContext pSecurityContext) {
        if (pCode != null && !pCode.isBlank()) {
            PromoCode lPromo = PromoCode.findByCode(pCode);
            if (lPromo == null) {
                return Response.ok(List.of()).build();
            }
            return Response.ok(List.of(lPromo)).build();
        }

        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        List<PromoCode> lPromoCodes = PromoCode.listAll();
        return Response.ok(lPromoCodes).build();
    }

    @POST
    @Transactional
    public Response create(PromoCode pPromoCode, @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        pPromoCode.createdAt = Instant.now().toString();
        if (pPromoCode.usedCount == null) {
            pPromoCode.usedCount = 0;
        }
        pPromoCode.persist();
        Log.infof("Promo code created. code=%s", pPromoCode.code);
        return Response.status(201).entity(pPromoCode).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long pId,
                           PromoCode pPartial,
                           @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        PromoCode lPromo = PromoCode.findById(pId);
        if (lPromo == null) {
            return Response.status(404).build();
        }
        if (pPartial.active != null) lPromo.active = pPartial.active;
        if (pPartial.usedCount != null) lPromo.usedCount = pPartial.usedCount;
        if (pPartial.maxUses != null) lPromo.maxUses = pPartial.maxUses;
        if (pPartial.validFrom != null) lPromo.validFrom = pPartial.validFrom;
        if (pPartial.validTo != null) lPromo.validTo = pPartial.validTo;
        if (pPartial.value != null) lPromo.value = pPartial.value;
        if (pPartial.type != null) lPromo.type = pPartial.type;
        if (pPartial.minOrder != null) lPromo.minOrder = pPartial.minOrder;
        Log.infof("Promo code updated. id=%d", pId);
        return Response.ok(lPromo).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long pId,
                           @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        PromoCode lPromo = PromoCode.findById(pId);
        if (lPromo == null) {
            return Response.status(404).build();
        }
        lPromo.delete();
        Log.infof("Promo code deleted. id=%d", pId);
        return Response.noContent().build();
    }
}
