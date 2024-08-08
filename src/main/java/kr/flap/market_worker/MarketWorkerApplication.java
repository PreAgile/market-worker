package kr.flap.market_worker;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@Slf4j
@EnableScheduling
@SpringBootApplication
public class MarketWorkerApplication {

	@PostConstruct
	public void started() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		log.info("Current JVM timezone: " + java.util.TimeZone.getDefault().getID());
	}

	public static void main(String[] args) {
		SpringApplication.run(MarketWorkerApplication.class, args);
	}
}
