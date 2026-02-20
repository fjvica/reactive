package com.example.demo.service;

import com.example.demo.model.ExternalUser;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;

/**
 * Servicio reactivo que consume una API externa usando WebClient inyectado.
 * Ahora incluye autenticación simulada, logging y reintentos configurados globalmente.
 */
@Service
public class ExternalUserService {

    private final WebClient webClient;

    // Inyectamos el WebClient global configurado en WebClientConfig
    public ExternalUserService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ExternalUser> getAllUsers() {
        return webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(ExternalUser.class)
                .timeout(Duration.ofSeconds(5))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)))
                .doOnSubscribe(sub -> System.out.println("🌐 Llamando a /users (GET)"));
    }

    public Mono<ExternalUser> getUserById(int id) {
        return webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(ExternalUser.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSubscribe(sub -> System.out.println("🌐 Llamando a /users/" + id));
    }

    public Mono<ExternalUser> createUser(ExternalUser user) {
        return webClient.post()
                .uri("/users")
                .bodyValue(user)
                .retrieve()
                .bodyToMono(ExternalUser.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSubscribe(sub -> System.out.println("🌐 Enviando POST /users"));
    }
}