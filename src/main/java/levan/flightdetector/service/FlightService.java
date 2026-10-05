package levan.flightdetector.service;

import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.FlightDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class FlightService {


    private final RestClient restClient;

    public FlightService(RestClient.Builder builder,
                         @Value("${adsb.url}") String baseUrl){
        this.restClient = builder.baseUrl(baseUrl).build();
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
