package fr.beacrea.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.beacrea.entity.CustomOrder;
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

@Path("/custom-orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomOrderResource {

    @Inject
    ObjectMapper mObjectMapper;

    // --- Routes client ---

    /**
     * Crée une demande de sur-mesure (authentification requise).
     */
    @POST
    @Transactional
    public Response create(CustomOrder pOrder, @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        pOrder.userId = pSecurityContext.getUserPrincipal().getName();
        pOrder.status = "received";
        pOrder.createdAt = Instant.now().toString();
        if (pOrder.messages == null) {
            pOrder.messages = mObjectMapper.createArrayNode();
        }
        pOrder.persist();
        Log.infof("Custom order created. customOrderId=%d, userId=%s", pOrder.id, pOrder.userId);
        return Response.status(201).entity(pOrder).build();
    }

    /**
     * Liste les sur-mesures de l'utilisateur connecté.
     */
    @GET
    public Response list(@Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        String lUserId = pSecurityContext.getUserPrincipal().getName();
        List<CustomOrder> lOrders = CustomOrder.findByUserId(lUserId);
        lOrders.sort(Comparator.comparing((CustomOrder o) -> o.createdAt).reversed());
        return Response.ok(lOrders).build();
    }

    /**
     * Détail d'une demande (l'utilisateur ne peut voir que les siennes).
     */
    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long pId, @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        CustomOrder lOrder = CustomOrder.findById(pId);
        if (lOrder == null) {
            return Response.status(404).build();
        }
        String lUserId = pSecurityContext.getUserPrincipal().getName();
        boolean lIsAdmin = pSecurityContext.isUserInRole("admin");
        if (!lIsAdmin && !lOrder.userId.equals(lUserId)) {
            return Response.status(403).entity(Map.of("error", "Accès refusé")).build();
        }
        return Response.ok(lOrder).build();
    }

    /**
     * Envoie un message sur une demande (client ou admin).
     */
    @POST
    @Path("/{id}/messages")
    @Transactional
    public Response addMessage(@PathParam("id") Long pId, MessageRequest pMessage,
                               @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        CustomOrder lOrder = CustomOrder.findById(pId);
        if (lOrder == null) {
            return Response.status(404).build();
        }

        String lUserId = pSecurityContext.getUserPrincipal().getName();
        boolean lIsAdmin = pSecurityContext.isUserInRole("admin");
        if (!lIsAdmin && !lOrder.userId.equals(lUserId)) {
            return Response.status(403).entity(Map.of("error", "Accès refusé")).build();
        }

        ObjectNode lNewMessage = mObjectMapper.createObjectNode();
        lNewMessage.put("id", "msg_" + System.currentTimeMillis());
        lNewMessage.put("from", lIsAdmin ? "admin" : "client");
        lNewMessage.put("authorName", pMessage.authorName() != null ? pMessage.authorName() : "Utilisateur");
        lNewMessage.put("content", pMessage.content());
        lNewMessage.put("createdAt", Instant.now().toString());

        ArrayNode lMessages = lOrder.messages != null
                ? (ArrayNode) lOrder.messages
                : mObjectMapper.createArrayNode();
        lMessages.add(lNewMessage);
        lOrder.messages = lMessages;

        Log.infof("Message added to custom order. customOrderId=%d, from=%s", pId, lNewMessage.get("from").asText());
        return Response.ok(lOrder).build();
    }

    // --- Routes admin ---

    /**
     * Liste toutes les demandes de sur-mesure (admin uniquement).
     */
    @GET
    @Path("/admin/all")
    public Response listAll(@Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        List<CustomOrder> lOrders = CustomOrder.listAll();
        lOrders.sort(Comparator.comparing((CustomOrder o) -> o.createdAt).reversed());
        return Response.ok(lOrders).build();
    }

    /**
     * Met à jour le statut, le devis ou les notes d'une demande (admin).
     */
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long pId, CustomOrder pPartial,
                           @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        CustomOrder lOrder = CustomOrder.findById(pId);
        if (lOrder == null) {
            return Response.status(404).build();
        }
        if (pPartial.status != null) lOrder.status = pPartial.status;
        if (pPartial.quote != null) lOrder.quote = pPartial.quote;
        if (pPartial.notes != null) lOrder.notes = pPartial.notes;
        Log.infof("Custom order updated. customOrderId=%d, status=%s", pId, lOrder.status);
        return Response.ok(lOrder).build();
    }

    public record MessageRequest(String content, String authorName) {}
}
