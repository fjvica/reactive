package com.example.demo.controller;

import com.example.demo.service.SensorService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SensorSSEController {

    private final SensorService sensorService;

    public SensorSSEController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    /**
     * Endpoint SSE que transmite datos de temperatura en tiempo real.
     *
     * ⚙️ Clave:
     * - produces = TEXT_EVENT_STREAM_VALUE -> indica al cliente que recibirá eventos continuos
     * - Cada elemento emitido por el Flux se envía al cliente sin cerrar la conexión
     *
     * 🌐 Ejemplo: GET http://localhost:8080/stream/temperature
     */
    @GetMapping(value = "/stream/temperature", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamTemperature() {
        return sensorService.temperatureStream();
    }
}

