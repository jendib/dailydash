package org.bgamard.dailydash.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.bgamard.dailydash.client.HomeAssistantClient;
import org.bgamard.dailydash.config.Config;
import org.bgamard.dailydash.model.HomeAssistantData;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/home-assistant")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class HomeAssistantResource {
    @Inject
    @RestClient
    HomeAssistantClient homeAssistantClient;
    
    @Inject
    Config config;

    @GET
    public HomeAssistantData get() {
        String authorization = "Bearer " + config.homeassistant().token();
        int co2Level = Integer.parseInt(homeAssistantClient.getState(authorization, "sensor.awair_2nd_edition_10736_carbon_dioxide").state);
        double indoorTemperature = Double.parseDouble(homeAssistantClient.getState(authorization, "sensor.awair_2nd_edition_10736_temperature").state);
        double outdoorTemperature = Double.parseDouble(homeAssistantClient.getState(authorization, "sensor.temperature_28").state);
        String tempoToday = homeAssistantClient.getState(authorization, "sensor.rte_tempo_couleur_actuelle").state;
        String tempoTomorrow = homeAssistantClient.getState(authorization, "sensor.rte_tempo_prochaine_couleur").state;
        return new HomeAssistantData(co2Level, indoorTemperature, outdoorTemperature, tempoToday, tempoTomorrow);
    }
}
