package com.example.demo.service;

import com.example.demo.model.ExternalUser;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Servicio que consume una API REST externa de forma reactiva usando WebClient.
 * En este caso, se conecta a JSONPlaceholder (https://jsonplaceholder.typicode.com/users)
 * para demostrar el uso de WebClient en llamadas GET y POST, manejo de errores, reintentos,
 * y timeouts.
 */
@Service
public class ExternalUserService {

    private final WebClient webClient;

    /**
     * Constructor con inyección de un WebClient configurado con base URL.
     */
    public ExternalUserService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com") // URL base de la API externa
                .build();
    }

    // =============================================================
    // 1️⃣ Obtener todos los usuarios (GET)
    // =============================================================

    /**
     * Llama al endpoint externo /users y devuelve un flujo (Flux) de usuarios.
     * Se incluye manejo de errores HTTP, reintentos automáticos y timeout.
     */
    public Flux<ExternalUser> getAllUsers() {
        return webClient.get()
                .uri("/users")                              // ruta completa -> https://jsonplaceholder.typicode.com/users
                .retrieve()                                 // ejecuta la llamada HTTP
                .onStatus(HttpStatusCode::is4xxClientError, // manejo de errores 4xx
                        response -> Mono.error(new RuntimeException("Error del cliente al obtener usuarios")))
                .onStatus(HttpStatusCode::is5xxServerError, // manejo de errores 5xx
                        response -> Mono.error(new RuntimeException("Error del servidor externo")))
                .bodyToFlux(ExternalUser.class)             // transforma el cuerpo JSON en objetos ExternalUser
                .timeout(Duration.ofSeconds(5))             // cancela si tarda más de 5 segundos
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))) // reintenta 2 veces con retraso
                .doOnSubscribe(sub -> System.out.println("Iniciando petición GET /users"))
                .doOnNext(u -> System.out.println("Usuario recibido: " + u.getName()))
                .doOnError(err -> System.err.println("Error en getAllUsers(): " + err.getMessage()))
                .doOnComplete(() -> System.out.println("Finalizada la petición GET /users"));
    }

    // =============================================================
    // 2️⃣ Obtener un usuario por ID (GET)
    // =============================================================

    /**
     * Llama al endpoint externo /users/{id} y devuelve un único usuario (Mono).
     * Maneja errores específicos de red o JSON.
     */
    public Mono<ExternalUser> getUserById(int id) {
        return webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(ExternalUser.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorResume(WebClientResponseException.class, e -> {
                    System.err.println("Error HTTP al obtener usuario " + id + ": " + e.getStatusCode());
                    return Mono.empty();
                })
                .onErrorResume(Exception.class, e -> {
                    System.err.println("Error general en getUserById: " + e.getMessage());
                    return Mono.empty();
                })
                .doOnSuccess(u -> System.out.println("Usuario obtenido correctamente: " + u))
                .doOnTerminate(() -> System.out.println("Petición /users/" + id + " completada"));
    }

    // =============================================================
    // 3️⃣ Crear un usuario (POST)
    // =============================================================

    /**
     * Envía un nuevo usuario al endpoint externo /users.
     * Este endpoint en JSONPlaceholder no guarda los datos realmente,
     * pero devuelve un objeto simulado con un ID.
     */
    public Mono<ExternalUser> createUser(ExternalUser user) {
        return webClient.post()
                .uri("/users")
                .bodyValue(user)                             // cuerpo JSON del POST
                .retrieve()
                .bodyToMono(ExternalUser.class)
                .timeout(Duration.ofSeconds(3))
                .doOnSubscribe(sub -> System.out.println("Enviando POST con usuario: " + user))
                .doOnSuccess(u -> System.out.println("Usuario creado (simulado): " + u))
                .onErrorResume(e -> {
                    System.err.println("Error en createUser(): " + e.getMessage());
                    return Mono.just(new ExternalUser(0, "fallback", "unknown", "none", "none"));
                });
    }

    // =============================================================
    // 4️⃣ Combinar datos externos con flujo local
    // =============================================================

    /**
     * Ejemplo de combinación de datos externos con un flujo interno.
     * Simula combinar usuarios externos con usuarios locales.
     */
    public Flux<String> combineWithLocalData() {
        Flux<String> localUsers = Flux.just("LocalUser1", "LocalUser2");

        Flux<String> externalUsers = getAllUsers()
                .map(ExternalUser::getName)
                .take(2); // solo los dos primeros para simplificar

        return Flux.zip(localUsers, externalUsers, (local, external) ->
                        "Local: " + local + " | Externo: " + external)
                .doOnNext(System.out::println);
    }
}