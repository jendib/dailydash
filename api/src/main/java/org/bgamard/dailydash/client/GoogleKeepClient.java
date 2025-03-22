package org.bgamard.dailydash.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.model.ChangeRequest;
import org.bgamard.dailydash.model.ChangeResponse;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "googlekeep")
@ApplicationScoped
@Path("/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface GoogleKeepClient {
    @POST
    @Path("v1/changes")
    ChangeResponse changes(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @HeaderParam(HttpHeaders.USER_AGENT) String userAgent,
            ChangeRequest request
    );
}
