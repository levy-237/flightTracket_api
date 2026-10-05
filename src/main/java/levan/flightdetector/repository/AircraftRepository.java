package levan.flightdetector.repository;

import levan.flightdetector.model.Aircraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AircraftRepository extends JpaRepository<Aircraft,Long> {
    Optional<Aircraft> findByHex(String hex);
    List<Aircraft> findByHexIn(Collection<String> hexes);

}
