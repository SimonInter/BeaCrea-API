package fr.beacrea.resource;

import fr.beacrea.entity.StockAlert;
import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.Instant;
import java.util.List;

@Path("/backInStockAlerts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class StockAlertResource {

    @GET
    public Response list(@QueryParam("productId") Long pProductId,
                         @QueryParam("size") String pSize,
                         @QueryParam("userEmail") String pUserEmail,
                         @QueryParam("notified") Boolean pNotified) {
        List<StockAlert> lAlerts = StockAlert.listAll();
        if (pProductId != null) lAlerts = lAlerts.stream().filter(a -> pProductId.equals(a.productId)).toList();
        if (pSize != null) lAlerts = lAlerts.stream().filter(a -> pSize.equals(a.size)).toList();
        if (pUserEmail != null) lAlerts = lAlerts.stream().filter(a -> pUserEmail.equals(a.userEmail)).toList();
        if (pNotified != null) lAlerts = lAlerts.stream().filter(a -> pNotified.equals(a.notified)).toList();
        return Response.ok(lAlerts).build();
    }

    @POST
    @Transactional
    public Response create(StockAlert pAlert) {
        pAlert.createdAt = Instant.now().toString();
        pAlert.persist();
        Log.infof("Stock alert created. productId=%d, size=%s, userEmail=%s",
                pAlert.productId, pAlert.size, pAlert.userEmail);
        return Response.status(201).entity(pAlert).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long pId) {
        StockAlert lAlert = StockAlert.findById(pId);
        if (lAlert == null) {
            return Response.status(404).build();
        }
        lAlert.delete();
        Log.infof("Stock alert deleted. id=%d", pId);
        return Response.noContent().build();
    }
}
