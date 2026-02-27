package com.example.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;

@SpringBootApplication
public class StarterApplication {

    @Autowired
    private ConfigurableEnvironment environment;

    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printStartupInfo() {

        System.out.println("\n================ APPLICATION STARTUP INFO ================");

        // Profiles
        System.out.println("Active Profiles: ");
        for (String profile : environment.getActiveProfiles()) {
            System.out.println(" - " + profile);
        }

        // Server
        System.out.println("Server Port: " + environment.getProperty("server.port", "8080"));

        // Datasource
        System.out.println("Datasource URL: " + environment.getProperty("spring.datasource.url"));
        System.out.println("Datasource User: " + environment.getProperty("spring.datasource.username"));

        // JPA
        System.out.println("Hibernate DDL: " + environment.getProperty("spring.jpa.hibernate.ddl-auto"));

        // Liquibase
        System.out.println("Liquibase Enabled: " + environment.getProperty("spring.liquibase.enabled"));
        System.out.println("Liquibase Changelog: " + environment.getProperty("spring.liquibase.change-log"));

        // Java & JVM
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("JVM Name: " + System.getProperty("java.vm.name"));
        System.out.println("PID: " + ManagementFactory.getRuntimeMXBean().getPid());

        System.out.println("===========================================================\n");
    }
}
