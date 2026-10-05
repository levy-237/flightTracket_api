package levan.flightdetector.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String callsign;

    private Instant lastSeen;

    @ManyToOne
    private Aircraft aircraft;

    public Flight() {
    }

    public Flight(
            String callsign,
            Instant lastSeen,
            Aircraft aircraft
    ) {
        this.callsign = callsign;
        this.lastSeen = lastSeen;
        this.aircraft = aircraft;
    }

    public Long getId() {
        return id;
    }

    public String getCallsign() {
        return callsign;
    }


    public Instant getLastSeen() {
        return lastSeen;
    }

    public Aircraft getAircraft() {
        return aircraft;
    }

    public void setLastSeen(Instant lastSeen) {
        this.lastSeen = lastSeen;
    }
}