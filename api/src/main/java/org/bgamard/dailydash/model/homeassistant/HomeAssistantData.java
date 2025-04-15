package org.bgamard.dailydash.model.homeassistant;

public class HomeAssistantData {
    public int co2Level;
    public double indoorTemperature;
    public double outdoorTemperature;
    public String tempoToday;
    public String tempoTomorrow;

    public HomeAssistantData(int co2Level, double indoorTemperature, double outdoorTemperature, String tempoToday, String tempoTomorrow) {
        this.co2Level = co2Level;
        this.indoorTemperature = indoorTemperature;
        this.outdoorTemperature = outdoorTemperature;
        this.tempoToday = tempoToday;
        this.tempoTomorrow = tempoTomorrow;
    }
}
