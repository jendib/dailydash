package org.bgamard.dailydash.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dailydash.calendar")
public interface CalendarConfig {
    String url();
}
