package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Configuración global de WebClient para toda la aplicación.
 * Define base URL, cabeceras por defecto y filtros (autenticación y logging).
 */
@Configuration
public class WebClientConfig {

    /**
     * Crea un bean WebClient reutilizable con configuración base.
     * Los servicios pueden inyectarlo directamente.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("https://jsonplaceholder.typicode.com") // URL base de la API externa
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                // Añadimos filtros personalizados
                .filter(logRequest())     // log de peticiones
                .filter(logResponse())    // log de respuestas
                .filter(authHeader("Bearer token-demo-1234")) // cabecera de autenticación simulada
                .build();
    }

    // =============================================================
    // Filtros personalizados
    // =============================================================

    /**
     * Log detallado de cada petición enviada.
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            System.out.println("➡️  [REQUEST] " + request.method() + " " + request.url());
            request.headers().forEach((name, values) ->
                    System.out.println("   " + name + ": " + String.join(",", values))
            );
            return Mono.just(request);
        });
    }

    /**
     * Log detallado de cada respuesta recibida.
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            System.out.println("⬅️  [RESPONSE] Status: " + response.statusCode());
            return Mono.just(response);
        });
    }

    /**
     * Añade cabecera de autenticación personalizada.
     */
    private ExchangeFilterFunction authHeader(String token) {
        return (request, next) -> {
            // Añadimos el header Authorization a la petición
            return next.exchange(
                    ClientRequest.from(request)
                            .headers(headers -> headers.setBearerAuth(token))
                            .build()
            );
        };
    }
}
