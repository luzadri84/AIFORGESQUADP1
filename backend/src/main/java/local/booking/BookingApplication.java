package local.booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class BookingApplication extends SpringBootServletInitializer {
    @org.springframework.context.annotation.Bean
    java.time.Clock clock() { return java.time.Clock.systemUTC(); }
    public static void main(String[] args) {
        SpringApplication.run(BookingApplication.class, args);
    }
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BookingApplication.class);
    }
}
