package project.luckybooky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LuckybookyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LuckybookyApplication.class, args);
	}

}
