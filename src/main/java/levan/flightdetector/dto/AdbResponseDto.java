package levan.flightdetector.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import  java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdbResponseDto(List<FlightDto> ac) {
}
