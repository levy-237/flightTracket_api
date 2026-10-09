package levan.flightdetector.scheduler;

import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.LiveFlightDto;
import levan.flightdetector.service.FlightBroadcastService;
import levan.flightdetector.service.FlightPersistanceService;
import levan.flightdetector.service.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;

@Component
public class FlightScheduler {

    private static final Logger log = LoggerFactory.getLogger(FlightScheduler.class);

    private static final double VIENNA_LAT = 48.1103;
    private static final double VIENNA_LON = 16.5697;
    private static final int RADIUS = 175;

    public final FlightService flightService;
    public final FlightBroadcastService flightBroadcastService;
    public final FlightPersistanceService flightPersistanceService;


    public FlightScheduler(FlightBroadcastService flightBroadcastService, FlightService flightService,FlightPersistanceService flightPersistanceService){
        this.flightBroadcastService= flightBroadcastService;
        this.flightService = flightService;
        this.flightPersistanceService = flightPersistanceService;
    }

    @Scheduled(cron = "0 59 23 * * *", zone = "Europe/Vienna")
    public void cleanupFlights() {
        log.info("Nightly cleanup started.");
        flightPersistanceService.deleteAllFlights();
        log.info("Nightly cleanup completed: all flight positions and flights deleted.");
    }

    @Scheduled(
            fixedDelayString = "${adsb.poll-interval-ms}"
    )
    public void pollFlights(){
        long started = System.nanoTime();
        String stage = "fetching ADS-B data";
        try {
            log.info("Flight poll started: fetching ADS-B data.");
            AdbResponseDto flights = flightService.getNearbyFlights(VIENNA_LAT, VIENNA_LON, RADIUS);
            if (flights == null || flights.ac() == null) {
                throw new IllegalStateException("ADS-B response is missing the aircraft list");
            }

            stage = "persisting snapshot";
            log.info("ADS-B fetch completed: {} aircraft; persisting snapshot.", flights.ac().size());
            List<LiveFlightDto> ourParsedData = flightPersistanceService.persistSnapshot(flights);

            stage = "broadcasting flights";
            log.info("Snapshot persisted: broadcasting {} flights.", ourParsedData.size());
            flightBroadcastService.broadcastAircraft(ourParsedData);

            log.info("Aircraft data fetched successfully and published to /topic/flights in {} ms.",
                    (System.nanoTime() - started) / 1_000_000);
        } catch (HttpClientErrorException.TooManyRequests exception) {
            log.warn("Too many requests to the aircraft API; skipping this update.");
        } catch (RuntimeException exception) {
            log.error("Flight poll failed while {} after {} ms; the next scheduled poll will retry.",
                    stage, (System.nanoTime() - started) / 1_000_000, exception);
        }
    }

}
