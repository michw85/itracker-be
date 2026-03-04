package de.upteams.tasktracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main class of the App
 */
@SpringBootApplication
@EnableAsync
public class TaskTrackerApplication {

    /**
     * Method that starts the App and deploys it to nested Tomcat
     */
    public static void main(String[] args) {
        SpringApplication.run(TaskTrackerApplication.class, args);
    }
}
