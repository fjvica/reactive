package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class CombineService {

    // =============================================================
    // COMBINE LATEST - TEMPERATURA Y HUMEDAD
    // =============================================================
    /**
     * Cada sensor emite valores en intervalos diferentes.
     * combineLatest genera un nuevo valor cada vez que cualquiera de los sensores emite,
     * combinando siempre los ultimos valores de ambos.
     *
     * Flujo simulado:
     * Temperatura: 20, 21, 22 ... cada 1 segundo
     * Humedad: 40, 42, 43 ... cada 1.5 segundos
     *
     * Emisiones:
     * 20°C + 40% -> primer valor
     * 21°C + 40% -> cuando temperatura emite de nuevo
     * 21°C + 42% -> cuando humedad emite
     * 22°C + 42% -> siguiente temperatura
     */
    public Flux<String> combineLatestSensors() {
        Flux<Integer> temperature = Flux.just(20, 21, 22)
                .delayElements(Duration.ofSeconds(1));
        Flux<Integer> humidity = Flux.just(40, 42, 43)
                .delayElements(Duration.ofMillis(1500));

        return Flux.combineLatest(temperature, humidity,
                        (t, h) -> "Temperatura: " + t + "°C, Humedad: " + h + "%")
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("combineLatest complete"));
    }

    // =============================================================
    // WITH LATEST FROM - TEMPERATURA COMO TRIGGER
    // =============================================================
    /**
     * withLatestFrom emite solo cuando el flujo principal (temperatura) emite.
     * Toma el ultimo valor conocido del flujo secundario (humedad) en ese momento.
     *
     * Ejemplo:
     * Temperatura: 20, 21, 22 cada 1 segundo -> triggers principales
     * Humedad: 40, 42, 43 cada 1.5 segundos -> valor secundario
     *
     * Emisiones:
     * 20°C + 40% -> primer trigger
     * 21°C + 40% -> segundo trigger (humedad aun no cambio)
     * 22°C + 42% -> tercer trigger, ahora con humedad actualizada
     */
    public Flux<String> withLatestFromSensors() {
        Flux<Integer> temperature = Flux.just(20, 21, 22)
                .delayElements(Duration.ofSeconds(1));
        Flux<Integer> humidity = Flux.just(40, 42, 43)
                .delayElements(Duration.ofMillis(1500));

        return temperature.withLatestFrom(humidity,
                        (t, h) -> "Temperatura: " + t + "°C, Humedad: " + h + "%")
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("withLatestFrom complete"));
    }

    // =============================================================
    // AMB - PRIMER SENSOR QUE EMITE
    // =============================================================
    /**
     * amb toma solo el flujo que emite primero y descarta el otro.
     * Ideal para redundancia o respuestas multiples, donde solo importa la mas rapida.
     *
     * Flujo simulado:
     * Sensor A: emite 22°C con delay 300ms
     * Sensor B: emite 21°C con delay 100ms
     *
     * Resultado: se toma el valor de Sensor B (el primero)
     */
    public Flux<String> ambSensors() {
        Flux<String> sensorA = Flux.just("Sensor A: 22°C").delayElements(Duration.ofMillis(300));
        Flux<String> sensorB = Flux.just("Sensor B: 21°C").delayElements(Duration.ofMillis(100));

        return Flux.firstWithSignal(sensorA, sensorB)
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("amb complete"));
    }

    // =============================================================
    // REDUCE - PROMEDIO DE TEMPERATURAS
    // =============================================================
    /**
     * reduce acumula todos los elementos y devuelve un resultado final.
     * Aqui calculamos el promedio de temperaturas simuladas.
     *
     * Flujo: 20, 21, 22
     * Resultado final: promedio = (20+21+22)/3 = 21
     */
    public Mono<Double> reduceAverageTemperature() {
        Flux<Integer> temperature = Flux.just(20, 21, 22);

        return temperature
                .reduce((a, b) -> a + b) // suma total
                .map(sum -> sum / 3.0)   // calculo promedio
                .doOnSuccess(avg -> System.out.println("Promedio temperatura: " + avg + "°C"));
    }

    // =============================================================
    // SCAN - SUMA ACUMULATIVA DE TEMPERATURAS
    // =============================================================
    /**
     * scan acumula valores y emite cada paso intermedio.
     * Aqui mostramos la suma acumulativa de temperaturas.
     *
     * Flujo: 20, 21, 22
     * Emisiones:
     * Paso 1: 20
     * Paso 2: 20+21=41
     * Paso 3: 41+22=63
     */
    public Flux<Integer> scanSumTemperature() {
        Flux<Integer> temperature = Flux.just(20, 21, 22);

        return temperature
                .scan((a, b) -> a + b)
                .doOnNext(step -> System.out.println("Suma acumulativa: " + step))
                .doOnComplete(() -> System.out.println("scan complete"));
    }
}

