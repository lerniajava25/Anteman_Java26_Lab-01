package elpriser.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyData {

    @JsonProperty("SEK_per_kwh")
    private double sekPerKwh;

    @JsonProperty("EUR_per_kwh")
    private double eurPerKwh;

    @JsonProperty("EXR")
    private double exr;

    @JsonProperty("time_start")
    private String timeStart;

    @JsonProperty("time_end")
    private double timeEnd;


    public HourlyData() {
        // Konstruktor som krävs för Jackson
    }

    private double hourlyRateInOren() {
        return sekPerKwh * 100.0;
    }

    public double getSekPerKwh() {
        return sekPerKwh;
    }

    public String getTimeStart() {
        return timeStart;
    }
    public double getTimeEnd() {
        return timeEnd;
    }

    private String getHourFormatted() {
        if (timeStart  != null && timeStart.length() >= 16) {
            return timeStart.substring(11, 16);
        }
        return timeStart;
    }

    @Override
    public String toString() {
        return String.format("%s: %.2f öre/kWh", getHourFormatted(), hourlyRateInOren());
    }
}
