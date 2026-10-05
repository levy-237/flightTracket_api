package levan.flightdetector.repository;

import levan.flightdetector.model.Aircraft;
import levan.flightdetector.model.Flight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight,Long> {
    List<Flight> findByAircraft_Id(Long aircraftId);
    Page<Flight> findByAircraft_Id(Long aircraftId, Pageable pageable);

    Optional<Flight> findTopByAircraftAndCallsignOrderByLastSeenDesc(
            Aircraft aircraft,
            String callsign
    );

    List<Flight> findByLastSeenAfterAndAircraft_HexIn(
            Instant cutoff,
            Collection<String> hexes
    );

    List<Flight> findByAircraft_HexIn(Collection<String> hexes);
    List<Flight> findByLastSeenAfter(Instant time);
}
