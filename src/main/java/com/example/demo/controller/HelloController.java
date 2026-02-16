package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

// Controlador de ejemplo simple para mostrar Mono.just
@RestController
@RequestMapping("/hello")
public class HelloController {

    @GetMapping
    public Mono<String> hello() {
        // Mono.just crea un publisher que emite exactamente un valor y se completa
        return Mono.just("Hola reactivo desde Spring WebFlux");
    }
}
