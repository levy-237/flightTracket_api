package levan.flightdetector.service;

import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.FlightDto;
import levan.flightdetector.model.Aircraft;
import levan.flightdetector.repository.AircraftRepository;
import levan.flightdetector.repository.FlightPositionRepository;
import levan.flightdetector.repository.FlightRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FlightPersistanceServiceTests {
    private AircraftRepository aircraft;
    private FlightRepository flights;
    private FlightPositionRepository positions;
    private FlightPersistanceService service;

    @BeforeEach
    void setUp() {
        aircraft = mock(AircraftRepository.class);
        flights = mock(FlightRepository.class);
        positions = mock(FlightPositionRepository.class);
        service = new FlightPersistanceService(aircraft, positions, flights);
    }

    @Test
    void deletesPositionsBeforeFlightsAndPreservesAircraft() {
        service.deleteAllFlights();

        var order = inOrder(positions, flights);
        order.verify(positions).deleteAllInBatch();
        order.verify(flights).deleteAllInBatch();
        verifyNoInteractions(aircraft);
    }

    @Test
    void doesNotDeleteFlightsWhenPositionDeletionFails() {
        var failure = new IllegalStateException("Position deletion failed");
        doThrow(failure).when(positions).deleteAllInBatch();

        assertSame(failure, assertThrows(IllegalStateException.class, service::deleteAllFlights));

        verifyNoInteractions(flights, aircraft);
    }

    @ParameterizedTest
    @MethodSource("metadataUpdates")
    void fillsOnlyMissingAircraftMetadata(
            String storedRegistration, String storedType, String registration, String type,
            String expectedRegistration, String expectedType) {
        var existing = new Aircraft("abc123", storedRegistration, storedType);
        when(aircraft.findByHexIn(any())).thenReturn(List.of(existing));

        var result = service.persistSnapshot(new AdbResponseDto(List.of(
                observation("abc123", "AUA123", 48.0, 16.0, registration, type, null, null))));

        assertEquals(expectedRegistration, existing.getRegistration());
        assertEquals(expectedType, existing.getAircraftType());
        assertEquals(expectedRegistration, result.getFirst().registration());
        assertEquals(expectedType, result.getFirst().aircraftType());
        verify(aircraft).saveAll(argThat(saved -> !saved.iterator().hasNext()));
    }

    static Stream<Arguments> metadataUpdates() {
        return Stream.of(
                Arguments.of(null, null, " OE-ABC ", " A320 ", "OE-ABC", "A320"),
                Arguments.of("OE-OLD", "A319", "OE-NEW", "A320", "OE-OLD", "A319"),
                Arguments.of("OE-ABC", "A320", null, "  ", "OE-ABC", "A320"),
                Arguments.of("OE-ABC", "A320", "  ", null, "OE-ABC", "A320"),
                Arguments.of("OE-ABC", "A320", null, "A321", "OE-ABC", "A320"),
                Arguments.of("", "  ", " OE-ABC ", " A320 ", "OE-ABC", "A320"),
                Arguments.of("OE-ABC", null, "OE-NEW", "A320", "OE-ABC", "A320"),
                Arguments.of(null, "A320", "OE-ABC", "A321", "OE-ABC", "A320"),
                Arguments.of(null, null, null, "  ", null, null));
    }

    @Test
    void enrichesNewAircraftFromLaterObservationInSameSnapshot() {
        service.persistSnapshot(new AdbResponseDto(List.of(
                observation("abc123", null, null, null, "  ", null, null, null),
                observation("abc123", null, null, null, " OE-ABC ", "A320", null, null))));
        ArgumentCaptor<List<Aircraft>> captured = ArgumentCaptor.captor();
        verify(aircraft).saveAll(captured.capture());
        assertEquals(1, captured.getValue().size());
        assertEquals("OE-ABC", captured.getValue().getFirst().getRegistration());
    }

    private static FlightDto observation(String hex, String callsign, Double lat, Double lon,
                                         String registration, String type, Object altitude, Double speed) {
        return new FlightDto(hex, registration, type, callsign, lat, lon,
                altitude, null, speed, null, null, null,
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null);
    }
}
