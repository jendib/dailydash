package org.bgamard.dailydash.resource;

import io.github.rukins.gpsoauth.Auth;
import io.github.rukins.gpsoauth.model.AccessTokenRequestParams;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.client.GoogleKeepClient;
import org.bgamard.dailydash.config.Config;
import org.bgamard.dailydash.model.ChangeRequest;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Path("/shopping-list")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShoppingListResource {
    @Inject
    Config config;

    @Inject
    @RestClient
    GoogleKeepClient googleKeepClient;
    
    @GET
    public List<String> get() throws Exception {
        AccessTokenRequestParams accessTokenRequestParams = AccessTokenRequestParams
                .withDefaultValues()
                .masterToken(config.googlekeep().masterToken())
                .app("com.google.android.keep")
                .scopes("oauth2:https://www.googleapis.com/auth/memento https://www.googleapis.com/auth/reminders")
                .build();

        String accessToken = new Auth().getAccessToken(accessTokenRequestParams).getAccessToken();

        return googleKeepClient.changes(
                "OAuth " + accessToken,
                "x-gkeepapi (https://github.com/rukins/gkeepapi-java)",
                new ChangeRequest(new ChangeRequest.RequestHeader(config.googlekeep().sessionId())))
                .nodes
                .stream()
                .filter(node -> node.type.equals("LIST_ITEM")
                        && node.parentId.equals(config.googlekeep().nodeId())
                        && !node.checked)
                .map(node -> node.text)
                .toList();
    }
}
