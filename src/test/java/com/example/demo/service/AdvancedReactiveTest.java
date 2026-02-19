package com.example.demo.service;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;

public class AdvancedReactiveTest {

    // =============================================================
    // TEST DE FLUJO INFINITO CON INTERVAL Y VIRTUAL TIME
    // =============================================================
    /**
     * Simula un flujo de ticks cada segundo usando Flux.interval.
     * withVirtualTime permite "avanzar el tiempo" para no esperar realmente.
     */
    @Test
    void testIntervalWithVirtualTime() {
        StepVerifier.withVirtualTime(() -> Flux.interval(Duration.ofSeconds(1))
                        .take(5)) // solo tomamos 5 emisiones
                .thenAwait(Duration.ofSeconds(5)) // avanzamos el tiempo virtual
                .expectNext(0L, 1L, 2L, 3L, 4L) // esperamos los ticks
                .expectComplete()
                .verify();
    }

    // =============================================================
    // TEST DE BACKPRESSURE CON LIMITRATE
    // =============================================================
    /**
     * Flux.range genera muchos elementos rápidamente.
     * limitRate simula el control de backpressure solicitando solo 50 a la vez.
     */
    @Test
    void testBackpressureLimitRate() {
        Flux<Integer> numbers = Flux.range(1, 1000)
                .limitRate(50);

        StepVerifier.create(numbers)
                .expectNextCount(1000) // verificamos que se emiten todos los 1000
                .expectComplete()
                .verify();
    }

    // =============================================================
    // TEST DE BACKPRESSURE CON ONBACKPRESSUREBUFFER
    // =============================================================
    /**
     * Flujo que produce valores muy rápido.
     * onBackpressureBuffer permite almacenar valores en un buffer mientras se procesan.
     */
    @Test
    void testBackpressureBuffer() {
        Flux<Integer> fastProducer = Flux.range(1, 100)
                .onBackpressureBuffer(20); // buffer de 20 elementos

        StepVerifier.create(fastProducer)
                .expectNextCount(100)
                .expectComplete()
                .verify();
    }

    // =============================================================
    // TEST DE BACKPRESSURE CON ONBACKPRESSUREDROP
    // =============================================================
    /**
     * onBackpressureDrop descarta elementos que no se pueden procesar inmediatamente.
     * Útil cuando solo nos interesa los últimos valores.
     */
    @Test
    void testBackpressureDrop() {
        Flux<Integer> fastProducer = Flux.range(1, 100)
                .onBackpressureDrop(i -> System.out.println("Descartado: " + i));

        StepVerifier.create(fastProducer)
                .expectNextCount(100) // todos los elementos se producen, aunque algunos podrían ser descartados en tiempo real
                .expectComplete()
                .verify();
    }

    // =============================================================
    // TEST DE BACKPRESSURE CON ONBACKPRESSURELATEST
    // =============================================================
    /**
     * onBackpressureLatest descarta todos los valores intermedios y mantiene solo el último disponible.
     * Útil cuando solo necesitamos el valor más reciente de un sensor.
     */
    @Test
    void testBackpressureLatest() {
        Flux<Integer> fastProducer = Flux.range(1, 100)
                .onBackpressureLatest();

        StepVerifier.create(fastProducer)
                .expectNextCount(100)
                .expectComplete()
                .verify();
    }
}

