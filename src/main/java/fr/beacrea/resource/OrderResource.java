package fr.beacrea.resource;

import com.fasterxml.jackson.databind.JsonNode;
import fr.beacrea.entity.Order;
import fr.beacrea.entity.Product;
import io.quarkus.logging.Log;
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

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @GET
    public Response list(
            @QueryParam("id") Long pId,
            @QueryParam("userId") String pUserId,
            @Context SecurityContext pSecurityContext) {

        // Liste complète : réservée aux admins
        if (pId == null && pUserId == null) {
            if (!pSecurityContext.isUserInRole("admin")) {
                return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
            }
            return Response.ok(Order.listAll()).build();
        }

        if (pId != null) {
            Order lOrder = Order.findById(pId);
            return Response.ok(lOrder != null ? List.of(lOrder) : List.of()).build();
        }

        List<Order> lOrders = Order.findByUserId(pUserId);
        lOrders.sort(Comparator.comparing((Order o) -> o.createdAt).reversed());
        return Response.ok(lOrders).build();
    }

    /**
     * Crée une commande ET décrémente le stock pour chaque article, dans la même transaction.
     * Si le stock est insuffisant pour un article, la commande est refusée (rollback automatique).
     */
    @POST
    @Transactional
    public Response create(Order pOrder, @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }

        // Vérification du paiement Stripe avant de créer la commande
        if (pOrder.paymentIntentId == null || pOrder.paymentIntentId.isBlank()) {
            return Response.status(400).entity(Map.of("error", "PaymentIntent manquant")).build();
        }
        if (!PaymentResource.isPaymentSucceeded(pOrder.paymentIntentId)) {
            Log.warnf("Order creation refused: payment not succeeded. intentId=%s", pOrder.paymentIntentId);
            return Response.status(402).entity(Map.of("error", "Paiement non confirmé")).build();
        }

        // Décrémentation du stock pour chaque article
        if (pOrder.items != null) {
            for (JsonNode lItem : pOrder.items) {
                long lProductId = lItem.path("productId").asLong();
                String lSize = lItem.path("size").asText();
                int lQty = lItem.path("quantity").asInt(1);

                Product lProduct = Product.findById(lProductId);
                if (lProduct == null) {
                    return Response.status(404)
                        .entity(Map.of("error", "Produit introuvable : " + lProductId)).build();
                }

                JsonNode lStock = lProduct.stock;
                int lCurrentStock = lStock != null ? lStock.path(lSize).asInt(0) : 0;
                if (lCurrentStock < lQty) {
                    return Response.status(400)
                        .entity(Map.of("error", "Stock insuffisant pour " + lProduct.name + " taille " + lSize)).build();
                }

                // Met à jour le stock via le JsonNode (compatible avec le schéma actuel)
                ((com.fasterxml.jackson.databind.node.ObjectNode) lProduct.stock).put(lSize, lCurrentStock - lQty);
            }
        }

        pOrder.id = System.currentTimeMillis();
        pOrder.createdAt = Instant.now().toString();
        if (pOrder.status == null) {
            pOrder.status = "confirmed";
        }
        pOrder.persist();

        Log.infof("Order created. orderId=%d, userId=%s, total=%s", pOrder.id, pOrder.userId, pOrder.total);
        return Response.status(Response.Status.CREATED).entity(pOrder).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response updateStatus(@PathParam("id") long pId, Order pPartial,
                                 @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        Order lOrder = Order.findById(pId);
        if (lOrder == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (pPartial.status != null) {
            lOrder.status = pPartial.status;
            Log.infof("Order status updated. orderId=%d, newStatus=%s", pId, pPartial.status);
        }
        return Response.ok(lOrder).build();
    }
}
