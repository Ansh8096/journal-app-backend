package net.engineerAnsh.journalApp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Slf4j
@SpringBootApplication
@EnableMongoAuditing
public class JournalApp {

    // whenever we want to run our application, we will run this file, because this is one and only entry point (main func.) present in our entire project...
	public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(JournalApp.class, args);

    }
}

