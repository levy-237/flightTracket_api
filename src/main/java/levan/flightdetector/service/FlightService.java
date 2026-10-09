package levan.flightdetector.service;

import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.FlightDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Service
public class FlightService {


    private final RestClient restClient;

    public FlightService(RestClient.Builder builder,
                         @Value("${adsb.url}") String baseUrl,
                         @Value("${adsb.connect-timeout-ms:5000}") int connectTimeoutMs,
                         @Value("${adsb.read-timeout-ms:15000}") int readTimeoutMs){
        if (connectTimeoutMs <= 0 || readTimeoutMs <= 0) {
            throw new IllegalArgumentException("ADS-B HTTP timeouts must be positive");
        }
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = builder.baseUrl(baseUrl).requestFactory(requestFactory).build();
    }

    public AdbResponseDto getNearbyFlights(double lat, double lon, int radius){

        return restClient
                .get()
                .uri(
                        "/v2/point/{lat}/{lon}/{radius}",
                        lat,
                        lon,
                        radius
                )
                .retrieve()
                .body(AdbResponseDto.class);
    }

//    TETSING
//    THIS
//    public FlightDto getAircrafts(){
//        return restClient
//                .get()
//                .uri(
//                        apiKey + "/v2/point/{lat}/{lon}/{radius}"
//                )
//                .retrieve()
//                .body(FlightDto.class);
//    }
}
