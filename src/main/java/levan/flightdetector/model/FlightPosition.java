package levan.flightdetector.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class FlightPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double latitude;
    private Double longitude;

    private Integer altitude;

    private Double groundSpeed;
    private Double track;

//    private Integer verticalRate;
    private Integer barometricRate;
    private Instant recordedAt;


    @ManyToOne
    private Flight flight;

    public FlightPosition() {
    }

    public FlightPosition(
            Double latitude,
            Double longitude,
            Integer altitude,
            Double groundSpeed,
            Double track,
            Integer barometricRate,
//            Integer verticalRate,
            Instant recordedAt,
            Flight flight
    ) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.groundSpeed = groundSpeed;
        this.track = track;
        this.barometricRate = barometricRate;
//        this.verticalRate = verticalRate;
        this.recordedAt = recordedAt;
        this.flight = flight;
    }

    public Long getId() {
        return id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Integer getAltitude() {
        return altitude;
    }
    public Integer getBarometricRate(){
        return  barometricRate;
    }
    public Double getGroundSpeed() {
        return groundSpeed;
    }

    public Double getTrack() {
        return track;
    }

//    public Integer getVerticalRate() {
//        return verticalRate;
//    }

    public Instant getRecordedAt() {
        return recordedAt;
    }

    public Flight getFlight() {
        return flight;
    }
}