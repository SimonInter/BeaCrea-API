package fr.beacrea.resource;

import fr.beacrea.entity.Newsletter;
import io.quarkus.logging.Log;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Path("/newsletters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class NewsletterResource {

    @GET
    public Response list(@QueryParam("email") String pEmail,
                         @QueryParam("active") Boolean pActive,
                         @Context SecurityContext pSecurityContext) {
        if (pEmail != null && !pEmail.isBlank()) {
            List<Newsletter> lResults = Newsletter.list("email", pEmail);
            return Response.ok(lResults).build();
        }

        if (!pSecurityContext.isUserInRole("admin")) {
            return Response.status(403).entity(Map.of("error", "Accès admin requis")).build();
        }
        List<Newsletter> lNewsletters = Newsletter.listAll();
        return Response.ok(lNewsletters).build();
    }

    @POST
    @Transactional
    public Response subscribe(Newsletter pNewsletter) {
        Newsletter lExisting = Newsletter.findByEmail(pNewsletter.email);
        if (lExisting != null) {
            return Response.ok(lExisting).build();
        }
        pNewsletter.subscribedAt = Instant.now().toString();
        pNewsletter.active = true;
        pNewsletter.persist();
        Log.infof("Newsletter subscription created. email=%s", pNewsletter.email);
        return Response.status(201).entity(pNewsletter).build();
    }

    @PATCH
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long pId, Newsletter pPartial) {
        Newsletter lNewsletter = Newsletter.findById(pId);
        if (lNewsletter == null) {
            return Response.status(404).build();
        }
        if (pPartial.active != null) lNewsletter.active = pPartial.active;
        Log.infof("Newsletter subscription updated. id=%d, active=%b", pId, lNewsletter.active);
        return Response.ok(lNewsletter).build();
    }
}
