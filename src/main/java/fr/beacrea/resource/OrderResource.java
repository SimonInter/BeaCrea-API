package fr.beacrea.resource;

import fr.beacrea.entity.Order;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Comparator;
import java.util.List;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    @GET
    public Response list(
            @QueryParam("id") Long pId,
            @QueryParam("userId") String pUserId) {
        if (pId != null) {
            Order lOrder = Order.findById(pId);
            return Response.ok(lOrder != null ? List.of(lOrder) : List.of()).build();
        }
        if (pUserId != null) {
            List<Order> lOrders = Order.findByUserId(pUserId);
            lOrders.sort(Comparator.comparing((Order o) -> o.createdAt).reversed());
            return Response.ok(lOrders).build();
        }
        return Response.ok(Order.listAll()).build();
    }

    @POST
    @Transactional
    public Response create(Order pOrder) {
        pOrder.persist();
        return Response.status(Response.Status.CREATED).entity(pOrder).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response updateStatus(@PathParam("id") long pId, Order pPartial) {
        Order lOrder = Order.findById(pId);
        if (lOrder == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (pPartial.status != null) {
            lOrder.status = pPartial.status;
        }
        return Response.ok(lOrder).build();
    }
}
