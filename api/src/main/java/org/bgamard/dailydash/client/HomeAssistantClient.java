package org.bgamard.dailydash.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.model.State;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "homeassistant")
@ApplicationScoped
@RegisterProvider(HomeAssistantFilter.class)
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HomeAssistantClient {
    @GET
    @Path("states/{id}")
    State getState(@PathParam("id") String id);
}
