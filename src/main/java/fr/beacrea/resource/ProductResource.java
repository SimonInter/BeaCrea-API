package fr.beacrea.resource;

import fr.beacrea.entity.Product;
import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;
import java.util.Map;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    // --- Routes publiques ---

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

    // --- Routes admin (token JWT + rôle "admin" requis) ---

    @POST
    @Transactional
    public Response create(Product pProduct, @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        pProduct.persist();
        Log.infof("Product created. productId=%d, name=%s", pProduct.id, pProduct.name);
        return Response.status(Response.Status.CREATED).entity(pProduct).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") long pId, Product pProduct,
                           @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        Product lExisting = Product.findById(pId);
        if (lExisting == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        pProduct.id = pId;
        Product.getEntityManager().merge(pProduct);
        Log.infof("Product updated. productId=%d", pId);
        return Response.ok(pProduct).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patch(@PathParam("id") long pId, Product pPartial,
                          @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        Product lProduct = Product.findById(pId);
        if (lProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (pPartial.name != null) lProduct.name = pPartial.name;
        if (pPartial.price != null) lProduct.price = pPartial.price;
        if (pPartial.stock != null) lProduct.stock = pPartial.stock;
        if (pPartial.description != null) lProduct.description = pPartial.description;
        if (pPartial.images != null) lProduct.images = pPartial.images;
        if (pPartial.badge != null) lProduct.badge = pPartial.badge;
        Log.infof("Product patched. productId=%d", pId);
        return Response.ok(lProduct).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") long pId, @Context SecurityContext pSecurityContext) {
        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        Product lProduct = Product.findById(pId);
        if (lProduct == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        lProduct.delete();
        Log.infof("Product deleted. productId=%d", pId);
        return Response.noContent().build();
    }
}
