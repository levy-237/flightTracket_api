package levan.flightdetector.repository;

import levan.flightdetector.model.FlightPosition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightPositionRepository extends JpaRepository<FlightPosition,Long> {
    List<FlightPosition> findByFlight_Id(Long flightId);
    Page<FlightPosition> findByFlight_Id(Long flightId, Pageable pageable);
}
