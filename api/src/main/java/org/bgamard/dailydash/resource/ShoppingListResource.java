package org.bgamard.dailydash.resource;

import io.github.rukins.gkeepapi.client.GKeepClientWrapper;
import io.github.rukins.gkeepapi.model.gkeep.NodeRequest;
import io.github.rukins.gkeepapi.model.gkeep.NodeResponse;
import io.github.rukins.gkeepapi.model.gkeep.PlatformType;
import io.github.rukins.gkeepapi.model.gkeep.Timestamps;
import io.github.rukins.gkeepapi.model.gkeep.node.nodeobject.ListItemNode;
import io.github.rukins.gkeepapi.model.gkeep.requestheader.ClientVersion;
import io.github.rukins.gkeepapi.model.gkeep.requestheader.RequestHeader;
import io.github.rukins.gkeepapi.model.gkeep.requestheader.capability.Capability;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.config.Config;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Path("/shopping-list")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ShoppingListResource {
    @Inject
    Config config;
    
    @GET
    public List<String> get() throws Exception {
        GKeepClientWrapper client = new GKeepClientWrapper(config.googlekeep().masterToken());

        NodeResponse fullData = client.changes(NodeRequest.builder()
                .clientTimestamp(LocalDateTime.now(Timestamps.DEFAULT_ZONE_ID))
                .nodes(new ArrayList<>())
                .requestHeader(RequestHeader.builder()
                        .capabilities(Capability.DEFAULT_CAPABILITIES)
                        .clientLocale(RequestHeader.DEFAULT_LOCALE)
                        .clientPlatform(PlatformType.DEFAULT_PLATFORM_TYPE)
                        .clientSessionId(config.googlekeep().sessionId())
                        .clientVersion(ClientVersion.DEFAULT_CLIENT_VERSION)
                        .noteSupportedModelFeatures(RequestHeader.DEFAULT_NOTE_SUPPORTED_MODEL_FEATURES)
                        .build())
                .build());

        return fullData.getNodes()
                .stream()
                .filter(node -> node.getParentId().equals(config.googlekeep().nodeId()))
                .map(ListItemNode.class::cast)
                .filter(node -> !node.getChecked())
                .map(ListItemNode::getText)
                .toList();
    }
}
