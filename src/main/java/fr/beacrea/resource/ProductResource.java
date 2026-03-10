package fr.beacrea.resource;

import fr.beacrea.entity.Product;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @GET
    public List<Product> list(
            @QueryParam("category") String pCategory,
            @QueryParam("featured") Boolean pFeatured,
            @QueryParam("slug") String pSlug) {
        if (pSlug != null) {
            Product lProduct = Product.findBySlug(pSlug);
            return lProduct != null ? List.of(lProduct) : List.of();
        }
        if (pCategory != null) {
            return Product.findByCategory(pCategory);
        }
        if (Boolean.TRUE.equals(pFeatured)) {
            return Product.findFeatured();
        }
        return Product.listAll();
    }

    @GET
    @Path("/{id}")
    public Response getById(@PathParam("id") long pId) {
        Product lProduct = Product.findById(pId);
        if (lProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(lProduct).build();
    }
}
