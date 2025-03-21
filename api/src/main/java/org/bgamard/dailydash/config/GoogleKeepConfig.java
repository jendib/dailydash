package org.bgamard.dailydash.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dailydash.googlekeep")
public interface GoogleKeepConfig {
    String masterToken();
    String sessionId();
    String nodeId();
}
