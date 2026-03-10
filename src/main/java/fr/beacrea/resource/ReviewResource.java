package fr.beacrea.resource;

import fr.beacrea.entity.Review;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/reviews")
@Produces(MediaType.APPLICATION_JSON)
public class ReviewResource {

    @GET
    public List<Review> list(@QueryParam("productId") Long pProductId) {
        if (pProductId != null) {
            return Review.findByProductId(pProductId);
        }
        return Review.listAll();
    }
}
