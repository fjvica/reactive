package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;

@Service
public class SensorService {

    private final Random random = new Random();

    /**
     * Genera un flujo infinito de temperaturas simuladas.
     * Cada 1 segundo emite un valor nuevo.
     *
     * Características:
     * - Flux.interval -> genera ticks (0,1,2,3...) a intervalos regulares
     * - map() -> convierte cada tick en un valor de temperatura aleatorio
     * - never completes -> flujo infinito
     */
    public Flux<String> temperatureStream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    double temperature = 20 + random.nextDouble() * 10; // entre 20 y 30 ºC
                    return "[" + LocalTime.now() + "] Temperatura: " + String.format("%.2f", temperature) + "°C";
                })
                .doOnNext(value -> System.out.println("Emitido -> " + value));
    }
}

