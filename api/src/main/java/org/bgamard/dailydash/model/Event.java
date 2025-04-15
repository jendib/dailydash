package org.bgamard.dailydash.model;

import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.DateProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.Temporal;

public class Event implements Comparable<Event> {
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

    public boolean isFuture() {
        Temporal temporal = endDate == null ? startDate : endDate;
        
        if (temporal instanceof LocalDate date) {
            return LocalDate.now().isBefore(date) || LocalDate.now().isEqual(date);
        }
        if (temporal instanceof OffsetDateTime date) {
            return OffsetDateTime.now().isBefore(date);
        }
        return false;
    }

    @Override
    public int compareTo(Event o) {
        return toLocalDateTime().compareTo(o.toLocalDateTime());
    }
    
    private LocalDateTime toLocalDateTime() {
        if (startDate instanceof LocalDate date) {
            return date.atStartOfDay();
        }
        if (startDate instanceof OffsetDateTime date) {
            return date.toLocalDateTime();
        }
        throw new IllegalArgumentException("startDate is of unreconized type " + startDate.getClass());
    }
}
