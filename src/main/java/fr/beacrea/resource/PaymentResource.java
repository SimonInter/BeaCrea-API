package fr.beacrea.resource;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@Path("/payment")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class PaymentResource {

    @ConfigProperty(name = "stripe.secret-key")
    String mStripeSecretKey;

    @PostConstruct
    void init() {
        Stripe.apiKey = mStripeSecretKey;
    }

    /**
     * Crée un PaymentIntent Stripe et retourne le clientSecret au frontend.
     * Le montant est en euros (ex: 49.90), converti en centimes pour Stripe.
     */
    @POST
    @Path("/create-intent")
    public Response createPaymentIntent(Map<String, Object> pBody,
                                        @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }

        Object lAmountObj = pBody.get("amount");
        if (lAmountObj == null) {
            return Response.status(400).entity(Map.of("error", "Le montant est requis")).build();
        }

        try {
            // Stripe attend les montants en centimes (unité la plus petite)
            long lAmountInCents = Math.round(((Number) lAmountObj).doubleValue() * 100);
            if (lAmountInCents <= 0) {
                return Response.status(400).entity(Map.of("error", "Le montant doit être positif")).build();
            }

            PaymentIntentCreateParams lParams = PaymentIntentCreateParams.builder()
                    .setAmount(lAmountInCents)
                    .setCurrency("eur")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build()
                    )
                    .build();

            PaymentIntent lPaymentIntent = PaymentIntent.create(lParams);
            Log.infof("PaymentIntent created. intentId=%s, amountCents=%d", lPaymentIntent.getId(), lAmountInCents);

            return Response.ok(Map.of("clientSecret", lPaymentIntent.getClientSecret())).build();

        } catch (StripeException lException) {
            Log.errorf("Failed to create PaymentIntent. stripeError=%s", lException.getMessage());
            return Response.status(502).entity(Map.of("error", "Erreur Stripe : " + lException.getMessage())).build();
        } catch (Exception lException) {
            Log.errorf("Unexpected error creating PaymentIntent. error=%s", lException.getMessage());
            return Response.status(500).entity(Map.of("error", "Erreur interne du serveur")).build();
        }
    }

    /**
     * Vérifie qu'un PaymentIntent est bien payé (status = succeeded).
     * Utilisé par OrderResource avant de créer la commande.
     */
    public static boolean isPaymentSucceeded(String pPaymentIntentId) {
        try {
            PaymentIntent lPaymentIntent = PaymentIntent.retrieve(pPaymentIntentId);
            return "succeeded".equals(lPaymentIntent.getStatus());
        } catch (StripeException lException) {
            Log.errorf("Failed to verify PaymentIntent. intentId=%s, error=%s", pPaymentIntentId, lException.getMessage());
            return false;
        }
    }
}
