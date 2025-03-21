package org.bgamard.dailydash.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/weather")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class WeatherResource {
    // https://api.open-meteo.com/v1/forecast?latitude=45.7485&longitude=4.8467&daily=weather_code,temperature_2m_min,temperature_2m_max,precipitation_sum,wind_speed_10m_max,wind_gusts_10m_max&hourly=temperature_2m,precipitation,weather_code,wind_speed_10m,wind_gusts_10m,temperature_80m&models=best_match&current=precipitation,weather_code,temperature_2m,wind_speed_10m,is_day&minutely_15=precipitation&timezone=Europe%2FBerlin&forecast_minutely_15=24
    
    @GET
    public String get() {
        return "OK";
    }
}
