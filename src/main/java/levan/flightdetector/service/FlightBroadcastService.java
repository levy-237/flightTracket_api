package levan.flightdetector.service;


import levan.flightdetector.dto.AdbResponseDto;
import levan.flightdetector.dto.LiveFlightDto;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlightBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;

    public FlightBroadcastService(
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastAircraft(
            List<LiveFlightDto> flights
    ) {
        messagingTemplate.convertAndSend(
                "/topic/flights",
                flights
        );
    }
}