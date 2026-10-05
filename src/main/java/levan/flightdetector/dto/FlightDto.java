package levan.flightdetector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record FlightDto(

                         String hex,

                         @JsonProperty("r")
                         String registration,

                         @JsonProperty("t")
                         String aircraftType,

                         String flight,


                         Double lat,
                         Double lon,


                         @JsonProperty("alt_baro")
                         Object barometricAltitude,

                         @JsonProperty("alt_geom")
                         Integer geometricAltitude,


                         Double gs,
                         Integer ias,
                         Integer tas,
                         Double mach,


                         Double track,

                         @JsonProperty("true_heading")
                         Double trueHeading,

                         @JsonProperty("mag_heading")
                         Double magneticHeading,


                         @JsonProperty("baro_rate")
                         Integer barometricRate,

                         @JsonProperty("geom_rate")
                         Integer geometricRate,


                         String squawk,
                         String emergency,
                         String category,


                         @JsonProperty("nav_altitude_mcp")
                         Integer selectedAltitude,

                         @JsonProperty("nav_heading")
                         Double selectedHeading,

                         @JsonProperty("nav_modes")
                         List<String> navigationModes,

                         Double seen,

                         @JsonProperty("seen_pos")
                         Double positionSeen,



                         String type
) {

}
