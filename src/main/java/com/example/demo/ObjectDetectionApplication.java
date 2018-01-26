package com.example.demo;

import nu.pattern.OpenCV;
import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication

public class ObjectDetectionApplication {
    static {
        OpenCV.loadShared();
    }

    public static void main(String[] args) {
        SpringApplication.run(ObjectDetectionApplication.class, args);
    }

}
