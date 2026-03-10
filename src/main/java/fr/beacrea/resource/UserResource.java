package fr.beacrea.resource;

import fr.beacrea.entity.AppUser;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @GET
    public Response list(@QueryParam("email") String pEmail) {
        if (pEmail != null) {
            AppUser lUser = AppUser.findByEmail(pEmail);
            return Response.ok(lUser != null ? List.of(lUser) : List.of()).build();
        }
        return Response.ok(AppUser.listAll()).build();
    }

    @POST
    @Transactional
    public Response create(AppUser pUser) {
        pUser.persist();
        return Response.status(Response.Status.CREATED).entity(pUser).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String pId, AppUser pPartial) {
        AppUser lUser = AppUser.findById(pId);
        if (lUser == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (pPartial.firstName != null) lUser.firstName = pPartial.firstName;
        if (pPartial.lastName != null) lUser.lastName = pPartial.lastName;
        if (pPartial.addresses != null) lUser.addresses = pPartial.addresses;
        if (pPartial.wishlist != null) lUser.wishlist = pPartial.wishlist;
        return Response.ok(lUser).build();
    }
}
