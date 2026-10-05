package levan.flightdetector.controller;

import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.model.Aircraft;
import levan.flightdetector.model.Flight;
import levan.flightdetector.model.FlightPosition;
import levan.flightdetector.repository.AircraftRepository;
import levan.flightdetector.repository.FlightRepository;
import levan.flightdetector.repository.FlightPositionRepository;
import levan.flightdetector.service.FlightService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class Flights {

    private final FlightService flightService;
    private final AircraftRepository aircraftRepository;
    private final FlightRepository flightRepository;
    private final FlightPositionRepository flightPositionRepository;

    public Flights(
            FlightService flightService,
            AircraftRepository aircraftRepository,
            FlightRepository flightRepository,
            FlightPositionRepository flightPositionRepository) {
        this.aircraftRepository = aircraftRepository;
        this.flightRepository = flightRepository;
        this.flightPositionRepository = flightPositionRepository;
        this.flightService = flightService;
    }


    @GetMapping("/aircrafts")
    public PagedModel<Aircraft> getAircrafts(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(aircraftRepository.findAll(pageable));
    }

    @GetMapping("/aircrafts/{aircraftId}/flights")
    public PagedModel<Flight> getFlightsByAircraftId(
            @PathVariable("aircraftId") Long aircraftId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(flightRepository.findByAircraft_Id(aircraftId, pageable));
    }

    @GetMapping("/flights")
    public PagedModel<Flight> getFlights(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(flightRepository.findAll(pageable));
    }

    @GetMapping("/flights/{flightId}/positions")
    public PagedModel<FlightPosition> getFlightPositionsByFlightId(
            @PathVariable("flightId") Long flightId,
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(flightPositionRepository.findByFlight_Id(flightId, pageable));
    }

    @GetMapping("/flight-positions")
    public PagedModel<FlightPosition> getFlightPositions(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(flightPositionRepository.findAll(pageable));
    }


    // JUST TEST
    @GetMapping("/aircraft-details")
    public List<Map<String, Object>> getAircraftDetails() {
        return aircraftRepository.findAll().stream()
                .map(aircraft -> Map.<String, Object>of(
                        "aircraft", aircraft,
                        "flights", flightRepository.findByAircraft_Id(aircraft.getId()).stream()
                                .map(flight -> Map.<String, Object>of(
                                        "flight", flight,
                                        "positions", flightPositionRepository.findByFlight_Id(flight.getId())
                                ))
                                .toList()
                ))
                .toList();
    }

}
