package com.purbon.kafka.streams.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ModelAndView health() {
        return new ModelAndView("forward:/actuator/health/kafkaStreams");
    }

    @GetMapping("/ready")
    public ModelAndView ready() {
        return health();
    }
}
