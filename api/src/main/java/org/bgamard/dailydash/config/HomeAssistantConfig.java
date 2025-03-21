package org.bgamard.dailydash.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dailydash.homeassistant")
public interface HomeAssistantConfig {
    String token();
}
