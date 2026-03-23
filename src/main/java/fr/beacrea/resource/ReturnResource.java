package fr.beacrea.resource;

import fr.beacrea.entity.OrderReturn;
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

@Path("/returns")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReturnResource {

    @Inject
    EmailService mEmailService;

    @GET
    public Response list(@QueryParam("userId") String pUserId,
                         @Context SecurityContext pSecurityContext) {
        if (pUserId != null && !pUserId.isBlank()) {
            if (pSecurityContext.getUserPrincipal() == null) {
                return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
            }
            List<OrderReturn> lReturns = OrderReturn.findByUserId(pUserId);
            lReturns.sort(Comparator.comparing((OrderReturn r) -> r.createdAt,
                    Comparator.nullsLast(Comparator.reverseOrder())));
            return Response.ok(lReturns).build();
        }

        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        List<OrderReturn> lReturns = OrderReturn.listAll();
        lReturns.sort(Comparator.comparing((OrderReturn r) -> r.createdAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return Response.ok(lReturns).build();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") Long pId,
                            @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        OrderReturn lReturn = OrderReturn.findById(pId);
        if (lReturn == null) {
            return Response.status(404).build();
        }
        return Response.ok(lReturn).build();
    }

    @POST
    @Transactional
    public Response create(OrderReturn pReturn,
                           @Context SecurityContext pSecurityContext) {
        if (pSecurityContext.getUserPrincipal() == null) {
            return Response.status(401).entity(Map.of("error", "Authentification requise")).build();
        }
        pReturn.status = "pending";
        pReturn.createdAt = Instant.now().toString();
        pReturn.persist();
        Log.infof("Return request created. orderId=%s, userId=%s", pReturn.orderId, pReturn.userId);
        mEmailService.sendReturnRequestToClient(pReturn);
        mEmailService.sendNewReturnToAdmin(pReturn);
        return Response.status(201).entity(pReturn).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long pId,
                           OrderReturn pPartial,
                           @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        OrderReturn lReturn = OrderReturn.findById(pId);
        if (lReturn == null) {
            return Response.status(404).build();
        }

        String lPreviousStatus = lReturn.status;

        if (pPartial.status != null) lReturn.status = pPartial.status;
        if (pPartial.refundAmount != null) lReturn.refundAmount = pPartial.refundAmount;
        if (pPartial.approvedAt != null) lReturn.approvedAt = pPartial.approvedAt;
        if (pPartial.rejectedAt != null) lReturn.rejectedAt = pPartial.rejectedAt;
        if (pPartial.refundedAt != null) lReturn.refundedAt = pPartial.refundedAt;
        if (pPartial.rejectionReason != null) lReturn.rejectionReason = pPartial.rejectionReason;

        Log.infof("Return updated. id=%d, status=%s", pId, lReturn.status);

        if (pPartial.status != null && !pPartial.status.equals(lPreviousStatus)) {
            switch (pPartial.status) {
                case "approved" -> mEmailService.sendReturnApprovedToClient(lReturn);
                case "rejected" -> mEmailService.sendReturnRejectedToClient(lReturn);
                case "refunded" -> mEmailService.sendReturnRefundedToClient(lReturn);
                default -> { }
            }
        }

        return Response.ok(lReturn).build();
    }
}
