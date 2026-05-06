package ch.frupp.tutorbot.bootstrap.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;

@Slf4j
@SpringBootApplication(scanBasePackages = "ch.frupp.tutorbot")
@Profile("demo-seed")
public class DemoDataSeederApplication implements ApplicationRunner {

    private final DemoDataSeeder demoDataSeeder;

    public DemoDataSeederApplication(DemoDataSeeder demoDataSeeder) {
        this.demoDataSeeder = demoDataSeeder;
    }

    static void main(String[] args) {
        SpringApplication app = new SpringApplication(DemoDataSeederApplication.class);
        app.setAdditionalProfiles("demo-seed");
        app.run(args).close();
        System.exit(0);
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("DemoDataSeederApplication started");
        try {
            // The actual seeding logic is handled by the DemoDataSeeder component
            // which is automatically picked up by Spring due to @Component annotation
            demoDataSeeder.run();
            log.info("Demo data seeding completed successfully");

        } catch (Exception e) {
            log.error("Error during demo data seeding", e);
            System.exit(1);
        }
    }
}


