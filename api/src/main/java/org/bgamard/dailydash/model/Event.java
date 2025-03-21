package org.bgamard.dailydash.model;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;

import java.time.LocalDate;
import java.time.temporal.Temporal;

public class Event {
    public String title;
    public String description;
    public String location;
    public Temporal startDate;
    public Temporal endDate;

    public static Event fromVEvent(VEvent vEvent) {
        Event event = new Event();
        event.title = getPropertyValue(vEvent.getSummary());
        event.description = getPropertyValue(vEvent.getDescription());
        event.location = getPropertyValue(vEvent.getLocation());
        event.startDate = getPropertyDate(vEvent.getDateTimeStart(), false);
        event.endDate = getPropertyDate(vEvent.getDateTimeEnd(), true);
        return event;
    }

    private static Temporal getPropertyDate(DateProperty<Temporal> date, boolean isEnd) {
        if (date == null) {
            return null;
        }
        
        if (isEnd) {
            if (date.getDate() instanceof LocalDate) {
                return ((LocalDate) date.getDate()).minusDays(1);
            }
        }
        
        return date.getDate();
    }

    private static String getPropertyValue(Property property) {
        return property != null ? property.getValue() : null;
    }
}
