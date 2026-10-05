package levan.flightdetector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlightdetectorApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightdetectorApplication.class, args);
	}

}
