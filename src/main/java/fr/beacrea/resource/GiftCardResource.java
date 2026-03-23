package fr.beacrea.resource;

import fr.beacrea.entity.GiftCard;
import fr.beacrea.service.EmailService;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Path("/gift-cards")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GiftCardResource {

    @Inject
    EmailService mEmailService;

    /**
     * Crée une carte cadeau (admin ou client connecté).
     */
    @POST
    @Transactional
    public Response create(GiftCard pCard, @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        if (pCard.code == null || pCard.code.isBlank()) {
            return Response.status(400).entity(Map.of("error", "Le code est requis")).build();
        }
        // Vérifier doublon de code
        if (GiftCard.findByCode(pCard.code) != null) {
            return Response.status(409).entity(Map.of("error", "Ce code existe déjà")).build();
        }
        pCard.balance = pCard.amount;
        pCard.active = true;
        pCard.createdAt = Instant.now().toString();
        pCard.usedAt = null;
        pCard.persist();
        mEmailService.sendGiftCardToRecipient(pCard);
        Log.infof("Gift card created. code=%s, amount=%.2f, recipientEmail=%s", pCard.code, pCard.amount, pCard.recipientEmail);
        return Response.status(201).entity(pCard).build();
    }

    /**
     * Liste toutes les cartes cadeaux (admin uniquement).
     */
    @GET
    public Response list(@QueryParam("code") String pCode,
                         @QueryParam("active") Boolean pActive,
                         @Context SecurityContext pSecurityContext) {
        // Si recherche par code (validate depuis le panier), pas besoin d'être admin
        if (pCode != null && !pCode.isBlank()) {
            GiftCard lCard = GiftCard.findByCode(pCode.toUpperCase());
            if (lCard == null || !Boolean.TRUE.equals(lCard.active)) {
                return Response.ok(List.of()).build();
            }
            return Response.ok(List.of(lCard)).build();
        }

        // Liste complète réservée admin
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        List<GiftCard> lCards = GiftCard.listAll();
        lCards.sort(Comparator.comparing((GiftCard c) -> c.createdAt).reversed());
        return Response.ok(lCards).build();
    }

    /**
     * Met à jour une carte cadeau (désactivation ou déduction solde).
     */
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long pId, GiftCard pPartial,
                           @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        GiftCard lCard = GiftCard.findById(pId);
        if (lCard == null) {
            return Response.status(404).build();
        }
        if (pPartial.active != null) lCard.active = pPartial.active;
        if (pPartial.balance != null) lCard.balance = pPartial.balance;
        if (pPartial.usedAt != null) lCard.usedAt = pPartial.usedAt;
        Log.infof("Gift card updated. id=%d, active=%b, balance=%.2f", pId, lCard.active, lCard.balance);
        return Response.ok(lCard).build();
    }

    /**
     * Récupère une carte cadeau par ID.
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long pId, @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        GiftCard lCard = GiftCard.findById(pId);
        if (lCard == null) {
            return Response.status(404).build();
        }
        return Response.ok(lCard).build();
    }
}
