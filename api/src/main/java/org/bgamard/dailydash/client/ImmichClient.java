package org.bgamard.dailydash.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bgamard.dailydash.model.immich.SearchRequest;
import org.bgamard.dailydash.model.immich.SearchResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "immich")
@ApplicationScoped
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ImmichClient {
    @POST
    @Path("search/metadata")
    SearchResponse searchAssets(@HeaderParam("x-api-key") String key, SearchRequest request);

    @GET
    @Path("/assets/{id}/thumbnail")
    Response viewAsset(@HeaderParam("x-api-key") String key, @PathParam("id") String id, @QueryParam("size") String size);
}
