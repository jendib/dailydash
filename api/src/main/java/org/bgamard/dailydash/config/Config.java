package org.bgamard.dailydash.config;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "dailydash")
public interface Config {
    GoogleKeepConfig googlekeep();
    CalendarConfig calendar();
    HomeAssistantConfig homeassistant();
}
