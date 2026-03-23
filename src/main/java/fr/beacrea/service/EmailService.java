package fr.beacrea.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.beacrea.entity.AppUser;
import fr.beacrea.entity.CustomOrder;
import fr.beacrea.entity.GiftCard;
import fr.beacrea.entity.Order;
import fr.beacrea.entity.OrderReturn;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";
    private static final String ADMIN_NAME = "Béatrice";
    private static final String SHOP_NAME = "BeaCrea";

    private static final Map<String, String> PROJECT_TYPE_LABELS = Map.of(
            "robe", "Robe",
            "tailleur", "Tailleur / Veste",
            "pantalon", "Pantalon / Jupe",
            "accessoire", "Accessoire",
            "maison", "Linge de maison",
            "autre", "Autre projet"
    );

    private static final Map<String, String> ORDER_STATUS_LABELS = Map.of(
            "pending", "En attente",
            "confirmed", "Confirmée",
            "shipped", "Expédiée",
            "delivered", "Livrée",
            "cancelled", "Annulée"
    );

    @ConfigProperty(name = "brevo.api-key")
    String mApiKey;

    @ConfigProperty(name = "brevo.sender-email")
    String mSenderEmail;

    @ConfigProperty(name = "brevo.sender-name")
    String mSenderName;

    @ConfigProperty(name = "brevo.admin-email")
    String mAdminEmail;

    @Inject
    ObjectMapper mObjectMapper;

    private final HttpClient mHttpClient = HttpClient.newHttpClient();

    // ─── Core ─────────────────────────────────────────────────────────────────

    /**
     * Appelle l'API Brevo en asynchrone (fire-and-forget).
     * Si la clé est DISABLED (mode dev sans config), loggue et ignore silencieusement.
     */
    private void send(String pToEmail, String pSubject, String pTextContent) {
        if ("DISABLED".equals(mApiKey) || mApiKey == null || mApiKey.isBlank()) {
            Log.debugf("[EMAIL-DEV] to=%s | subject=%s\n%s", pToEmail, pSubject, pTextContent);
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> lPayload = new HashMap<>();
                lPayload.put("sender", Map.of("name", mSenderName, "email", mSenderEmail));
                lPayload.put("to", List.of(Map.of("email", pToEmail)));
                lPayload.put("subject", pSubject);
                lPayload.put("textContent", pTextContent);

                String lJson = mObjectMapper.writeValueAsString(lPayload);

                HttpRequest lRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BREVO_API_URL))
                        .header("api-key", mApiKey)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(lJson))
                        .build();

                HttpResponse<String> lResponse = mHttpClient.send(lRequest, HttpResponse.BodyHandlers.ofString());

                if (lResponse.statusCode() >= 200 && lResponse.statusCode() < 300) {
                    Log.infof("Email sent. to=%s, subject=%s", pToEmail, pSubject);
                } else {
                    Log.warnf("Brevo non-2xx response. status=%d, to=%s, body=%s",
                            lResponse.statusCode(), pToEmail, lResponse.body());
                }
            } catch (Exception lException) {
                Log.errorf("Failed to send email. to=%s, error=%s", pToEmail, lException.getMessage());
            }
        });
    }

    // ─── Commandes boutique ───────────────────────────────────────────────────

    /** Confirmation de commande → client */
    public void sendOrderConfirmationToClient(Order pOrder) {
        if (pOrder.email == null) return;

        String lFirstName = fieldFromJson(pOrder.shippingAddress, "firstName");
        String lAddress = fieldFromJson(pOrder.shippingAddress, "address");
        String lPostalCode = fieldFromJson(pOrder.shippingAddress, "postalCode");
        String lCity = fieldFromJson(pOrder.shippingAddress, "city");
        String lItemsList = formatOrderItems(pOrder.items);
        String lTotal = pOrder.total != null ? String.format("%.2f", pOrder.total) : "—";
        String lShipping = pOrder.shippingCost != null && pOrder.shippingCost.doubleValue() > 0
                ? String.format("%.2f", pOrder.shippingCost) + " €" : "Offerte";

        send(pOrder.email,
                SHOP_NAME + " – Confirmation de votre commande #" + pOrder.id,
                String.join("\n",
                        "Bonjour " + lFirstName + ",",
                        "",
                        "Nous avons bien reçu votre commande #" + pOrder.id + ".",
                        "",
                        "Articles commandés :",
                        lItemsList,
                        "",
                        "Total : " + lTotal + " €",
                        "Livraison : " + lShipping,
                        "",
                        "Adresse de livraison :",
                        lAddress + ", " + lPostalCode + " " + lCity,
                        "",
                        "Nous vous tiendrons informé(e) de l'avancement de votre commande.",
                        "",
                        "Merci pour votre achat,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    /** Nouvelle commande → admin */
    public void sendNewOrderToAdmin(Order pOrder) {
        String lFirstName = fieldFromJson(pOrder.shippingAddress, "firstName");
        String lLastName = fieldFromJson(pOrder.shippingAddress, "lastName");
        String lTotal = pOrder.total != null ? String.format("%.2f", pOrder.total) : "—";
        String lItemsList = formatOrderItems(pOrder.items);

        send(mAdminEmail,
                "🛍️ Nouvelle commande #" + pOrder.id + " – " + pOrder.email,
                String.join("\n",
                        "Bonjour " + ADMIN_NAME + ",",
                        "",
                        "Une nouvelle commande vient d'être passée.",
                        "",
                        "Commande : #" + pOrder.id,
                        "Client : " + lFirstName + " " + lLastName + " (" + pOrder.email + ")",
                        "Total : " + lTotal + " €",
                        "",
                        "Articles :",
                        lItemsList,
                        "",
                        "Connectez-vous au back-office pour traiter cette commande."
                ));
    }

    /** Mise à jour du statut de commande → client */
    public void sendOrderStatusUpdateToClient(Order pOrder, String pNewStatus) {
        if (pOrder.email == null) return;

        String lFirstName = fieldFromJson(pOrder.shippingAddress, "firstName");
        String lStatusLabel = ORDER_STATUS_LABELS.getOrDefault(pNewStatus, pNewStatus);
        String lTrackingBlock = "";
        if ("shipped".equals(pNewStatus) && pOrder.trackingNumber != null && !pOrder.trackingNumber.isBlank()) {
            String lTrackingUrl = buildTrackingUrl(pOrder.carrier, pOrder.trackingNumber);
            lTrackingBlock = "\n\nNuméro de suivi : " + pOrder.trackingNumber
                    + (lTrackingUrl.isBlank() ? "" : "\nSuivre votre colis : " + lTrackingUrl);
        }

        String lStatusMessage = switch (pNewStatus) {
            case "confirmed" -> "Votre commande #" + pOrder.id + " a été confirmée et est en cours de préparation.";
            case "shipped"   -> "Votre commande #" + pOrder.id + " a été expédiée ! Vous recevrez votre colis dans les prochains jours." + lTrackingBlock;
            case "delivered" -> "Votre commande #" + pOrder.id + " a été livrée. Nous espérons que vous êtes satisfait(e) de votre achat !";
            case "cancelled" -> "Votre commande #" + pOrder.id + " a été annulée. N'hésitez pas à nous contacter si vous avez des questions.";
            default          -> "Votre commande #" + pOrder.id + " a été mise à jour : " + lStatusLabel + ".";
        };

        send(pOrder.email,
                SHOP_NAME + " – Commande #" + pOrder.id + " : " + lStatusLabel,
                String.join("\n",
                        "Bonjour " + lFirstName + ",",
                        "",
                        lStatusMessage,
                        "",
                        "Connectez-vous à votre espace client pour consulter les détails de votre commande.",
                        "",
                        "Cordialement,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    // ─── Sur-mesure ───────────────────────────────────────────────────────────

    /** Nouvelle demande sur-mesure → admin */
    public void sendCustomOrderReceivedToAdmin(CustomOrder pOrder) {
        String lTypeLabel = PROJECT_TYPE_LABELS.getOrDefault(pOrder.type, pOrder.type != null ? pOrder.type : "Projet");

        send(mAdminEmail,
                "Nouvelle demande sur-mesure – " + lTypeLabel + " par " + pOrder.userName,
                String.join("\n",
                        "Bonjour " + ADMIN_NAME + ",",
                        "",
                        pOrder.userName + " vient de soumettre une nouvelle demande sur-mesure.",
                        "",
                        "Type de projet : " + lTypeLabel,
                        "Description : " + (pOrder.description != null ? pOrder.description : "—"),
                        pOrder.estimatedBudget != null ? "Budget indicatif : " + pOrder.estimatedBudget + " €" : "",
                        pOrder.requestedDate != null ? "Délai souhaité : " + pOrder.requestedDate : "",
                        "",
                        "Connectez-vous au back-office pour consulter et répondre à cette demande."
                ).replace("\n\n\n", "\n\n"));
    }

    /** Confirmation de réception → client */
    public void sendCustomOrderReceivedToClient(CustomOrder pOrder) {
        if (pOrder.userEmail == null) return;
        String lTypeLabel = PROJECT_TYPE_LABELS.getOrDefault(pOrder.type, pOrder.type != null ? pOrder.type : "votre projet");

        send(pOrder.userEmail,
                SHOP_NAME + " – Votre demande sur-mesure a bien été reçue",
                String.join("\n",
                        "Bonjour " + pOrder.userName + ",",
                        "",
                        "Nous avons bien reçu votre demande sur-mesure pour : " + lTypeLabel + ".",
                        "",
                        "Vous recevrez un devis personnalisé prochainement. Vous pouvez suivre",
                        "l'avancement de votre demande dans votre espace client.",
                        "",
                        "À très bientôt,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    /** Nouveau message dans un fil sur-mesure */
    public void sendCustomOrderMessage(CustomOrder pOrder, String pFrom, String pContent) {
        String lTypeLabel = PROJECT_TYPE_LABELS.getOrDefault(pOrder.type, "votre projet");

        if ("admin".equals(pFrom) && pOrder.userEmail != null) {
            // Message de l'admin → client
            send(pOrder.userEmail,
                    SHOP_NAME + " – Vous avez un nouveau message",
                    String.join("\n",
                            "Bonjour " + pOrder.userName + ",",
                            "",
                            ADMIN_NAME + " vous a envoyé un message concernant votre projet \"" + lTypeLabel + "\".",
                            "",
                            "Message :",
                            "\"" + pContent + "\"",
                            "",
                            "Connectez-vous à votre espace client pour lire et répondre."
                    ));
        } else {
            // Message du client → admin
            send(mAdminEmail,
                    "Nouveau message de " + pOrder.userName + " – " + lTypeLabel,
                    String.join("\n",
                            "Bonjour " + ADMIN_NAME + ",",
                            "",
                            pOrder.userName + " vous a envoyé un message concernant sa demande sur-mesure (" + lTypeLabel + ").",
                            "",
                            "Message :",
                            "\"" + pContent + "\"",
                            "",
                            "Connectez-vous au back-office pour répondre."
                    ));
        }
    }

    /** Changement de statut sur-mesure (devis, en fabrication, terminé) → client */
    public void sendCustomOrderStatusUpdate(CustomOrder pOrder) {
        if (pOrder.userEmail == null) return;
        String lTypeLabel = PROJECT_TYPE_LABELS.getOrDefault(pOrder.type, "votre projet");

        switch (pOrder.status) {
            case "quoted" -> {
                String lPrice = pOrder.quote != null && pOrder.quote.has("amount")
                        ? pOrder.quote.get("amount").asText() + " €" : "voir votre espace client";
                send(pOrder.userEmail,
                        SHOP_NAME + " – Votre devis sur-mesure est disponible",
                        String.join("\n",
                                "Bonjour " + pOrder.userName + ",",
                                "",
                                "Votre devis pour le projet \"" + lTypeLabel + "\" est prêt !",
                                "",
                                "Montant proposé : " + lPrice,
                                "",
                                "Connectez-vous à votre espace client pour accepter ou refuser ce devis.",
                                "",
                                "Cordialement,",
                                ADMIN_NAME + " – " + SHOP_NAME
                        ));
            }
            case "in_progress" ->
                send(pOrder.userEmail,
                        SHOP_NAME + " – Votre projet est en cours de fabrication",
                        String.join("\n",
                                "Bonjour " + pOrder.userName + ",",
                                "",
                                "Votre projet \"" + lTypeLabel + "\" est maintenant en cours de fabrication !",
                                "",
                                ADMIN_NAME + " met tout son savoir-faire pour réaliser votre pièce unique.",
                                "Vous serez informé(e) dès qu'il sera prêt.",
                                "",
                                "À très bientôt,",
                                ADMIN_NAME + " – " + SHOP_NAME
                        ));
            case "done" ->
                send(pOrder.userEmail,
                        SHOP_NAME + " – Votre projet sur-mesure est terminé !",
                        String.join("\n",
                                "Bonjour " + pOrder.userName + ",",
                                "",
                                "Votre projet \"" + lTypeLabel + "\" est terminé !",
                                "",
                                ADMIN_NAME + " va vous contacter prochainement pour organiser la livraison ou le retrait.",
                                "",
                                "Merci de votre confiance,",
                                ADMIN_NAME + " – " + SHOP_NAME
                        ));
            default -> Log.debugf("No email template for custom order status: %s", pOrder.status);
        }
    }

    // ─── Inscription ──────────────────────────────────────────────────────────

    /** Email de bienvenue → nouvel utilisateur */
    public void sendWelcomeEmail(AppUser pUser) {
        send(pUser.email,
                "Bienvenue sur " + SHOP_NAME + " !",
                String.join("\n",
                        "Bonjour " + pUser.firstName + ",",
                        "",
                        "Votre compte " + SHOP_NAME + " a bien été créé.",
                        "",
                        "Vous pouvez dès maintenant explorer notre catalogue, ajouter des articles",
                        "à vos favoris et passer commande.",
                        "",
                        "À très bientôt,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    // ─── Utilitaires privés ───────────────────────────────────────────────────

    /** Construit l'URL de suivi selon le transporteur. */
    private String buildTrackingUrl(String pCarrier, String pTrackingNumber) {
        if (pCarrier == null || pTrackingNumber == null) return "";
        return switch (pCarrier.toLowerCase()) {
            case "colissimo"     -> "https://www.laposte.fr/outils/suivre-vos-envois?code=" + pTrackingNumber;
            case "chronopost"    -> "https://www.chronopost.fr/tracking-no-cms/suivi-page?listeNumerosLT=" + pTrackingNumber;
            case "mondial relay" -> "https://www.mondialrelay.fr/suivi-de-colis/?numcolis=" + pTrackingNumber;
            case "dhl"           -> "https://www.dhl.com/fr-fr/home/tracking.html?tracking-id=" + pTrackingNumber;
            default              -> "";
        };
    }

    /** Extrait un champ texte d'un JsonNode (shippingAddress, etc.). */
    private String fieldFromJson(JsonNode pNode, String pField) {
        if (pNode == null || !pNode.has(pField)) return "";
        return pNode.get(pField).asText("");
    }

    /** Formate la liste des articles d'une commande pour le corps de l'email. */
    private String formatOrderItems(JsonNode pItems) {
        if (pItems == null || !pItems.isArray() || pItems.isEmpty()) return "  (aucun article)";
        StringBuilder lBuilder = new StringBuilder();
        for (JsonNode lItem : pItems) {
            String lName = lItem.path("name").asText("Produit");
            String lSize = lItem.path("size").asText("");
            int lQty = lItem.path("quantity").asInt(1);
            double lPrice = lItem.path("price").asDouble(0);
            lBuilder.append(String.format("  - %s (%s) ×%d : %.2f €%n", lName, lSize, lQty, lPrice * lQty));
        }
        return lBuilder.toString().stripTrailing();
    }

    // ─── Alertes retour en stock ──────────────────────────────────────────────

    public void sendBackInStockToSubscriber(String pUserEmail, String pProductName, String pSize) {
        send(pUserEmail,
                SHOP_NAME + " – " + pProductName + " est de nouveau disponible !",
                String.join("\n",
                        "Bonjour,",
                        "",
                        "Bonne nouvelle ! Le produit que vous attendiez est de nouveau disponible :",
                        "",
                        pProductName + " – Taille " + pSize,
                        "",
                        "Rendez-vous vite sur notre boutique avant nouvelle rupture de stock.",
                        "",
                        "À bientôt,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    // ─── Retours ──────────────────────────────────────────────────────────────

    public void sendReturnRequestToClient(OrderReturn pReturn) {
        if (pReturn.email == null) return;
        int lItemCount = pReturn.items != null && pReturn.items.isArray() ? pReturn.items.size() : 0;
        send(pReturn.email,
                SHOP_NAME + " – Votre demande de retour a bien été reçue",
                String.join("\n",
                        "Bonjour " + (pReturn.userName != null ? pReturn.userName : "") + ",",
                        "",
                        "Nous avons bien reçu votre demande de retour pour " + lItemCount + " article" + (lItemCount > 1 ? "s" : "") + " de la commande #" + pReturn.orderId + ".",
                        "",
                        "Nous allons examiner votre demande et vous recontacterons prochainement.",
                        "Vous pouvez suivre l'état de votre retour dans votre espace client.",
                        "",
                        "Cordialement,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ));
    }

    public void sendNewReturnToAdmin(OrderReturn pReturn) {
        int lItemCount = pReturn.items != null && pReturn.items.isArray() ? pReturn.items.size() : 0;
        send(mAdminEmail,
                "↩️ Nouvelle demande de retour – Commande #" + pReturn.orderId,
                String.join("\n",
                        "Bonjour " + ADMIN_NAME + ",",
                        "",
                        (pReturn.userName != null ? pReturn.userName : pReturn.email) + " a soumis une demande de retour.",
                        "",
                        "Commande : #" + pReturn.orderId,
                        "Articles : " + lItemCount + " article" + (lItemCount > 1 ? "s" : ""),
                        "Motif : " + (pReturn.reason != null ? pReturn.reason : "Non précisé"),
                        pReturn.notes != null ? "Notes : " + pReturn.notes : "",
                        "",
                        "Connectez-vous au back-office pour traiter cette demande."
                ).replace("\n\n\n", "\n\n"));
    }

    public void sendReturnApprovedToClient(OrderReturn pReturn) {
        if (pReturn.email == null) return;
        send(pReturn.email,
                SHOP_NAME + " – Votre retour a été approuvé",
                String.join("\n",
                        "Bonjour " + (pReturn.userName != null ? pReturn.userName : "") + ",",
                        "",
                        "Votre demande de retour pour la commande #" + pReturn.orderId + " a été approuvée.",
                        "",
                        pReturn.refundAmount != null ? "Montant du remboursement : " + String.format("%.2f", pReturn.refundAmount) + " €" : "",
                        "",
                        "Veuillez nous retourner vos articles dans leur emballage d'origine dans les 7 jours.",
                        "Le remboursement sera effectué sous 5 à 10 jours ouvrés après réception.",
                        "",
                        "Cordialement,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ).replace("\n\n\n", "\n\n"));
    }

    public void sendReturnRejectedToClient(OrderReturn pReturn) {
        if (pReturn.email == null) return;
        send(pReturn.email,
                SHOP_NAME + " – Votre demande de retour",
                String.join("\n",
                        "Bonjour " + (pReturn.userName != null ? pReturn.userName : "") + ",",
                        "",
                        "Nous avons examiné votre demande de retour pour la commande #" + pReturn.orderId + ".",
                        "",
                        "Après vérification, nous ne sommes malheureusement pas en mesure de l'accepter.",
                        pReturn.rejectionReason != null ? "Motif : " + pReturn.rejectionReason : "",
                        "",
                        "N'hésitez pas à nous contacter si vous avez des questions.",
                        "",
                        "Cordialement,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ).replace("\n\n\n", "\n\n"));
    }

    public void sendReturnRefundedToClient(OrderReturn pReturn) {
        if (pReturn.email == null) return;
        send(pReturn.email,
                SHOP_NAME + " – Remboursement effectué",
                String.join("\n",
                        "Bonjour " + (pReturn.userName != null ? pReturn.userName : "") + ",",
                        "",
                        "Votre remboursement pour la commande #" + pReturn.orderId + " a bien été effectué.",
                        "",
                        pReturn.refundAmount != null ? "Montant remboursé : " + String.format("%.2f", pReturn.refundAmount) + " €" : "",
                        "",
                        "Le montant sera crédité sur votre moyen de paiement initial sous 3 à 5 jours ouvrés.",
                        "",
                        "Merci de votre confiance,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ).replace("\n\n\n", "\n\n"));
    }

    // ─── Cartes cadeaux ───────────────────────────────────────────────────────

    public void sendGiftCardToRecipient(GiftCard pCard) {
        if (pCard.recipientEmail == null) return;
        send(pCard.recipientEmail,
                SHOP_NAME + " – Vous avez reçu une carte cadeau de " + String.format("%.2f", pCard.amount) + " € !",
                String.join("\n",
                        "Bonjour,",
                        "",
                        "Vous avez reçu une carte cadeau " + SHOP_NAME + " d'une valeur de " + String.format("%.2f", pCard.amount) + " € !",
                        pCard.message != null && !pCard.message.isBlank() ? "\nMessage : \"" + pCard.message + "\"\n" : "",
                        "Votre code : " + pCard.code,
                        "",
                        "Utilisez ce code lors de votre paiement sur notre boutique en ligne.",
                        "Il est valable jusqu'à épuisement du solde.",
                        "",
                        "Bonne découverte,",
                        ADMIN_NAME + " – " + SHOP_NAME
                ).replace("\n\n\n", "\n\n"));
    }
}
