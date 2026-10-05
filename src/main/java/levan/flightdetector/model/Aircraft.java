package levan.flightdetector.model;

import jakarta.persistence.*;

@Entity
public class Aircraft {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long id;

    @Column(unique = true, nullable = false)
    private String hex;

    private String registration;

    private String aircraftType;

    public Aircraft() {
    }

    public Aircraft(
            String hex,
            String registration,
            String aircraftType
    ) {
        this.hex = hex;
        this.registration = registration;
        this.aircraftType = aircraftType;
    }

    public Long getId() {
        return id;
    }

    public String getHex(){
        return this.hex;
    }
    public String getRegistration() {
        return registration;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void updateMetadata(String registration, String aircraftType) {
        if ((this.registration == null || this.registration.isBlank())
                && registration != null && !registration.isBlank()) {
            this.registration = registration.trim();
        }
        if ((this.aircraftType == null || this.aircraftType.isBlank())
                && aircraftType != null && !aircraftType.isBlank()) {
            this.aircraftType = aircraftType.trim();
        }
    }

}
