package levan.flightdetector.service;

import jakarta.transaction.Transactional;
import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.FlightDto;
import levan.flightdetector.dto.LiveFlightDto;
import levan.flightdetector.model.Aircraft;
import levan.flightdetector.model.Flight;
import levan.flightdetector.model.FlightPosition;
import levan.flightdetector.repository.AircraftRepository;
import levan.flightdetector.repository.FlightPositionRepository;
import levan.flightdetector.repository.FlightRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlightPersistanceService {
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private  final FlightPositionRepository flightPositionRepository;

    public FlightPersistanceService(AircraftRepository aircraftRepository,FlightPositionRepository flightPositionRepository,FlightRepository flightRepository){
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.flightPositionRepository = flightPositionRepository;
    }

    @Transactional
    public List<LiveFlightDto> persistSnapshot(AdbResponseDto response){
        Instant now = Instant.now();

        Map<String, Aircraft> aircraftByHex =
                saveAircrafts(response);

        Map<String, Flight> currentFlights =
                saveFlights(
                        response,
                        aircraftByHex,
                        now
                );

        saveFlightPositions(
                response,
                currentFlights,
                now
        );

        return buildLiveFlights(
                response,
                currentFlights,
                aircraftByHex,
                now
        );
    }


    private List<LiveFlightDto> buildLiveFlights(
            AdbResponseDto response,
            Map<String, Flight> currentFlights,
            Map<String, Aircraft> aircraftByHex,
            Instant now
    ) {

        List<LiveFlightDto> liveFlights = new ArrayList<>();

        for (FlightDto dto : response.ac()) {

            if (dto.hex() == null || dto.flight() == null) {
                continue;
            }
            boolean parked =
                    "ground".equalsIgnoreCase(
                            String.valueOf(dto.barometricAltitude())
                    )
                            && dto.gs() != null
                            && dto.gs() < 3;

            if (parked) {
                continue;
            }

            String callsign = dto.flight().trim();

            if (callsign.isEmpty()) {
                continue;
            }

            String key = flightKey(
                    dto.hex(),
                    callsign
            );

            Flight flight = currentFlights.get(key);
            Aircraft aircraft = aircraftByHex.get(dto.hex());

            if (flight == null || aircraft == null) {
                continue;
            }

            LiveFlightDto liveFlight =
                    new LiveFlightDto(
                            flight.getId(),
                            aircraft.getId(),
                            aircraft.getHex(),
                            aircraft.getRegistration(),
                            aircraft.getAircraftType(),
                            callsign,
                            dto.lat(),
                            dto.lon(),
                            getAltitude(dto),
                            dto.gs(),
                            dto.track(),
                            dto.barometricRate(),
                            now
                    );

            liveFlights.add(liveFlight);
        }

        return liveFlights;
    }


    private Map<String, Aircraft> saveAircrafts(
            AdbResponseDto response
    ) {

        Set<String> incomingHexes = response.ac().stream()
                .map(FlightDto::hex)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        List<Aircraft> existingAircraft =
                aircraftRepository.findByHexIn(incomingHexes);


        Map<String, Aircraft> aircraftByHex =
                new HashMap<>();


        for (Aircraft aircraft : existingAircraft) {

            aircraftByHex.put(
                    aircraft.getHex(),
                    aircraft
            );
        }


        List<Aircraft> aircraftToCreate =
                new ArrayList<>();


        for (FlightDto dto : response.ac()) {

            if (dto.hex() == null) {
                continue;
            }

            Aircraft existing = aircraftByHex.get(dto.hex());
            if (existing != null) {
                // Existing entities are managed by the snapshot transaction.
                existing.updateMetadata(dto.registration(), dto.aircraftType());
                continue;
            }


            Aircraft aircraft = new Aircraft(
                    dto.hex(),
                    null,
                    null
            );
            aircraft.updateMetadata(dto.registration(), dto.aircraftType());


            aircraftByHex.put(
                    dto.hex(),
                    aircraft
            );

            aircraftToCreate.add(aircraft);
        }


        aircraftRepository.saveAll(
                aircraftToCreate
        );

        return aircraftByHex;
    }

    private Map<String, Flight> saveFlights(
            AdbResponseDto response,
            Map<String, Aircraft> aircraftByHex,
            Instant now
    ) {

        Instant cutoff =
                now.minusSeconds(3600);


        Set<String> incomingHexes =
                aircraftByHex.keySet();


        List<Flight> recentFlights =
                flightRepository
                        .findByLastSeenAfterAndAircraft_HexIn(
                                cutoff,
                                incomingHexes
                        );


        Map<String, Flight> flightsByKey =
                new HashMap<>();


        for (Flight flight : recentFlights) {

            String key = flightKey(
                    flight.getAircraft().getHex(),
                    flight.getCallsign()
            );

            Flight current =
                    flightsByKey.get(key);


            if (
                    current == null
                            || flight.getLastSeen()
                            .isAfter(current.getLastSeen())
            ) {
                flightsByKey.put(
                        key,
                        flight
                );
            }
        }


        Map<String, Flight> currentFlights =
                new HashMap<>();

        Map<String, Flight> flightsToSave =
                new HashMap<>();


        for (FlightDto dto : response.ac()) {

            if (
                    dto.hex() == null
                            || dto.flight() == null
            ) {
                continue;
            }


            String callsign =
                    dto.flight().trim();


            if (callsign.isEmpty()) {
                continue;
            }


            Aircraft aircraft =
                    aircraftByHex.get(dto.hex());


            if (aircraft == null) {
                continue;
            }


            String key =
                    flightKey(
                            dto.hex(),
                            callsign
                    );


            Flight flight =
                    flightsByKey.get(key);


            if (flight == null) {

                flight = new Flight(
                        callsign,
                        now,
                        aircraft
                );

            } else {

                flight.setLastSeen(now);
            }


            flightsByKey.put(
                    key,
                    flight
            );

            currentFlights.put(
                    key,
                    flight
            );

            flightsToSave.put(
                    key,
                    flight
            );
        }


        flightRepository.saveAll(
                flightsToSave.values()
        );


        return currentFlights;
    }


    private void saveFlightPositions(
            AdbResponseDto response,
            Map<String, Flight> currentFlights,
            Instant now
    ) {

        List<FlightPosition> positions =
                new ArrayList<>();


        for (FlightDto dto : response.ac()) {

            if (
                    dto.hex() == null
                            || dto.flight() == null
            ) {
                continue;
            }


            String callsign =
                    dto.flight().trim();


            if (callsign.isEmpty()) {
                continue;
            }


            String key =
                    flightKey(
                            dto.hex(),
                            callsign
                    );


            Flight flight =
                    currentFlights.get(key);


            if (flight == null) {
                continue;
            }


            if (
                    dto.lat() == null
                            || dto.lon() == null
            ) {
                continue;
            }


            FlightPosition position =
                    new FlightPosition(
                            dto.lat(),
                            dto.lon(),
                            getAltitude(dto),
                            dto.gs(),
                            dto.track(),
                            dto.barometricRate(),
                            now,
                            flight
                    );


            positions.add(position);
        }


        flightPositionRepository.saveAll(
                positions
        );
    }


    private String flightKey(
            String hex,
            String callsign
    ) {
        return hex + "|" + callsign;
    }


    private Integer getAltitude(
            FlightDto dto
    ) {

        Object altitude =
                dto.barometricAltitude();


        if (altitude instanceof Number number) {
            return number.intValue();
        }


        return null;
    }
}
