package org.bgamard.dailydash.client;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import org.bgamard.dailydash.config.Config;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class HomeAssistantFilter implements ClientRequestFilter {
    @Inject
    Config config;

    @Override
    public void filter(ClientRequestContext requestContext) {
        requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Bearer " + config.homeassistant().token());
    }
}
