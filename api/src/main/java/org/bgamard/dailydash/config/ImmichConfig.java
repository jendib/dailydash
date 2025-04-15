package org.bgamard.dailydash.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dailydash.immich")
public interface ImmichConfig {
    String key();
}
