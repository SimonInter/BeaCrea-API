package fr.beacrea.resource;

import fr.beacrea.entity.Category;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

@Path("/categories")
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    @GET
    public List<Category> list() {
        return Category.listAll();
    }
}
