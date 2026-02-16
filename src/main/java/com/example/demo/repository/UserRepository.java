package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Repositorio en memoria (sin MongoDB) para fines didacticos.
// Implementa operaciones basicas usando Mono/Flux para simular un repositorio reactivo.
@Repository
public class UserRepository {

    private final Map<String, User> store = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(0);

    // Devuelve todos los usuarios como Flux
    public Flux<User> findAll() {
        return Flux.fromIterable(store.values());
    }

    // Busca por id -> Mono (0..1)
    public Mono<User> findById(String id) {
        User u = store.get(id);
        return u != null ? Mono.just(u) : Mono.empty();
    }

    // Guarda o actualiza un usuario
    public Mono<User> save(User user) {
        if (user.getId() == null || user.getId().isBlank()) {
            String id = String.valueOf(counter.incrementAndGet());
            user.setId(id);
        }
        store.put(user.getId(), user);
        return Mono.just(user);
    }

    // Borra por id
    public Mono<Void> deleteById(String id) {
        store.remove(id);
        return Mono.empty();
    }
}

