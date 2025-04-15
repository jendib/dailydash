package org.bgamard.dailydash.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bgamard.dailydash.client.ImmichClient;
import org.bgamard.dailydash.config.Config;
import org.bgamard.dailydash.model.immich.SearchRequest;
import org.bgamard.dailydash.model.immich.SearchResponse;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Path("/photos")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PhotosResource {
    @Inject
    @RestClient
    ImmichClient immichClient;

    @Inject
    Config config;
    
    @GET
    @Path("latest")
    public List<String> latest() {
        SearchResponse response = immichClient.searchAssets(config.immich().key(), new SearchRequest());
        return response.assets.items.stream()
                .map(item -> item.id)
                .toList();
    }
    
    @GET
    @Path("{id}/preview")
    public Response preview(@PathParam("id") String id) {
        return immichClient.viewAsset(config.immich().key(), id, "preview");
    }
}
