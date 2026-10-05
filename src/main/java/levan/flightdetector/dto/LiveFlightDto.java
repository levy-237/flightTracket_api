package levan.flightdetector.dto;

import java.time.Instant;

public record LiveFlightDto(
        Long flightId,
        Long aircraftId,

        String hex,
        String registration,
        String aircraftType,

        String callsign,

        Double latitude,
        Double longitude,
        Integer altitude,

        Double groundSpeed,
        Double track,
        Integer verticalRate,

        Instant recordedAt
) {
}