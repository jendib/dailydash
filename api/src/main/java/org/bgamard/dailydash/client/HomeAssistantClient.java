package org.bgamard.dailydash.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.model.homeassistant.State;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "homeassistant")
@ApplicationScoped
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HomeAssistantClient {
    @GET
    @Path("states/{id}")
    State getState(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @PathParam("id") String id
    );
}
