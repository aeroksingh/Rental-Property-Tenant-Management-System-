package com.rentalmanagement.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RentalManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalManagementSystemApplication.class, args);
    }

}
