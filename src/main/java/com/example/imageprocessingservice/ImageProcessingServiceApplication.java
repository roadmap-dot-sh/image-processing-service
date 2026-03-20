package com.example.imageprocessingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ImageProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ImageProcessingServiceApplication.class, args);
    }

}
