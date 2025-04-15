package org.bgamard.dailydash.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import org.bgamard.dailydash.config.Config;
import org.bgamard.dailydash.model.Event;

import java.io.InputStream;
import java.net.URI;
import java.util.List;

@Path("/calendar")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CalendarResource {
    @Inject
    Config config;
    
    @GET
    public List<Event> get() throws Exception {
        try (InputStream inputStream = URI.create(config.calendar().url()).toURL().openStream()) {
            CalendarBuilder builder = new CalendarBuilder();
            Calendar calendar = builder.build(inputStream);
            return calendar.getComponents()
                    .stream()
                    .filter(VEvent.class::isInstance)
                    .map(VEvent.class::cast)
                    .map(Event::fromVEvent)
                    .filter(Event::isFuture)
                    .sorted()
                    .toList();
        }
    }
}
