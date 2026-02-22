package com.example.demo.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Servicio que muestra patrones avanzados de control y resiliencia en programación reactiva.
 * Todos los ejemplos son auto-contenidos y pensados para fines educativos.
 */
@Service
public class AdvancedReactivePatternsService {

    // =============================================================
    // 1️⃣ switchIfEmpty() - flujo alternativo si no hay elementos
    // =============================================================

    /**
     * Si el flujo principal no emite nada (por ejemplo, búsqueda vacía),
     * switchIfEmpty() permite definir un flujo alternativo.
     */
    public Flux<String> switchIfEmptyExample() {
        return Flux.<String>empty()
                .switchIfEmpty(Flux.just("sin", "resultados", "en", "fuente"))
                .doOnNext(s -> System.out.println("Elemento: " + s))
                .doOnComplete(() -> System.out.println("Finalizado switchIfEmptyExample()"));
    }

    // =============================================================
    // 2️⃣ retryWhen() avanzado - reintentos con backoff y filtrado
    // =============================================================

    /**
     * retryWhen() permite reintentar una operación que falla.
     * Podemos configurarlo para reintentar solo ciertos errores y con retraso progresivo.
     */
    public Flux<String> retryWhenExample() {
        return Flux.<String>create(sink -> {
                    System.out.println("Intento de operación externa...");
                    sink.error(new RuntimeException("Error temporal en servidor"));
                })
                .retryWhen(
                        Retry.backoff(3, Duration.ofSeconds(1)) // 3 reintentos con backoff
                                .filter(ex -> ex instanceof RuntimeException) // solo RuntimeExceptions
                                .onRetryExhaustedThrow((spec, signal) ->
                                        new RuntimeException("Reintentos agotados después de " + signal.totalRetries() + " intentos"))
                )
                .doOnError(e -> System.out.println("Error final: " + e.getMessage()))
                .onErrorResume(e -> Flux.just("fallback", "desde", "cache", "local"))
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("Finalizado retryWhenExample()"));
    }

    // =============================================================
    // 3️⃣ timeout() - cancelar flujos lentos
    // =============================================================

    /**
     * Si un flujo tarda más de lo permitido, timeout() lo cancela y emite error.
     * Podemos capturar ese error y devolver un fallback.
     */
    public Mono<String> timeoutExample() {
        return Mono.just("Procesando...")
                .delayElement(Duration.ofSeconds(3)) // simula operación lenta
                .timeout(Duration.ofSeconds(1))      // límite de tiempo
                .onErrorResume(throwable -> Mono.just("⚠️ Timeout -> usando fallback"))
                .doOnNext(System.out::println)
                .doOnTerminate(() -> System.out.println("Finalizado timeoutExample()"));
    }

    // =============================================================
    // 4️⃣ takeUntil() y takeWhile() - control de emisión
    // =============================================================

    /**
     * takeWhile() emite elementos mientras se cumpla la condición.
     * takeUntil() emite hasta que se cumpla la condición (inclusive).
     */
    public Flux<Integer> takeControlExample() {
        return Flux.range(1, 10)
                .takeWhile(n -> n < 5)  // emite 1,2,3,4
                .doOnNext(n -> System.out.println("takeWhile: " + n))
                .thenMany(
                        Flux.range(1, 10)
                                .takeUntil(n -> n == 3) // emite 1,2,3 y luego corta
                                .doOnNext(n -> System.out.println("takeUntil: " + n))
                )
                .doOnComplete(() -> System.out.println("Finalizado takeControlExample()"));
    }

    // =============================================================
    // 5️⃣ doFinally() - hook final siempre ejecutado
    // =============================================================

    /**
     * doFinally() se ejecuta SIEMPRE, independientemente de cómo termine el flujo:
     * - con éxito
     * - con error
     * - cancelado por timeout o takeUntil()
     */
    public Flux<String> doFinallyExample(boolean triggerError) {
        return Flux.just("A", "B", "C")
                .map(val -> {
                    if (triggerError && val.equals("B")) throw new RuntimeException("Error forzado");
                    return val;
                })
                .doOnNext(v -> System.out.println("Procesando: " + v))
                .doFinally(signalType -> System.out.println("✅ doFinally ejecutado con señal: " + signalType))
                .onErrorResume(e -> Flux.just("Recuperado", "tras", "error"));
    }

    // =============================================================
    // 6️⃣ concatMap() vs flatMapSequential() - control de orden
    // =============================================================

    /**
     * concatMap() -> mantiene el orden original, ejecuta flujos uno tras otro.
     * flatMapSequential() -> combina concurrencia y preserva orden de salida.
     */
    public Flux<String> orderedMappingExample() {
        return Flux.just("uno", "dos", "tres")
                .concatMap(v ->
                        Mono.just(v)
                                .delayElement(Duration.ofMillis(500))
                                .map(String::toUpperCase)
                )
                .doOnNext(v -> System.out.println("concatMap emite: " + v))
                .thenMany(
                        Flux.just("uno", "dos", "tres")
                                .flatMapSequential(v ->
                                        Mono.just(v)
                                                .delayElement(Duration.ofMillis(500))
                                                .map(String::toUpperCase)
                                )
                                .doOnNext(v -> System.out.println("flatMapSequential emite: " + v))
                )
                .doOnComplete(() -> System.out.println("Finalizado orderedMappingExample()"));
    }
}
