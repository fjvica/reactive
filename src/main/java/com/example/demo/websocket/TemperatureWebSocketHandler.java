package com.example.demo.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Random;

/**
 * Manejador de WebSocket reactivo.
 *
 * 🔁 Bidireccional:
 * - Recibe mensajes del cliente.
 * - Responde con la temperatura actual y la hora.
 */
@Component
public class TemperatureWebSocketHandler implements WebSocketHandler {

    private final Random random = new Random();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        // Flujo de entrada: mensajes del cliente
        Flux<String> input = session.receive()
                .map(msg -> msg.getPayloadAsText())
                .doOnNext(msg -> System.out.println("Mensaje recibido del cliente: " + msg));

        // Flujo de salida: temperatura periódica
        Flux<String> output = Flux.interval(Duration.ofSeconds(1))
                .map(tick -> {
                    double temperature = 20 + random.nextDouble() * 10;
                    return "Temperatura actual: " + String.format("%.2f", temperature) + "°C (" + LocalTime.now() + ")";
                });

        // Cada vez que el cliente envía algo, empezamos a enviar el flujo de temperatura
        return session.send(
                input.flatMap(msg -> output.map(session::textMessage))
        );
    }
}

