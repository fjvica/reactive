package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private UserRepository userRepository;
    private UserService userService;

    @BeforeEach
    void setup() {
        // Creamos un repositorio simulado
        userRepository = Mockito.mock(UserRepository.class);
        userService = new UserService(userRepository);
    }

    @Test
    void testFindAll() {
        // Preparamos datos de prueba
        User u1 = new User("1", "Alice", 25);
        User u2 = new User("2", "Bob", 17); // menor de edad, será filtrado

        when(userRepository.findAll()).thenReturn(Flux.just(u1, u2));

        // StepVerifier verifica el flujo
        StepVerifier.create(userService.findAll())
                .expectNextMatches(u -> u.getName().equals("ALICE")) // Alice transformada a mayus
                .expectComplete() // Solo Alice pasa el filtro
                .verify();
    }

    @Test
    void testFindByIdSuccess() {
        User u = new User("1", " Alice ", 30);
        when(userRepository.findById("1")).thenReturn(Mono.just(u));

        StepVerifier.create(userService.findById("1"))
                .expectNextMatches(user -> user.getName().equals("Alice")) // trim aplicado
                .expectComplete()
                .verify();
    }

    @Test
    void testFindByIdFallbackOnError() {
        User u = new User("1", "Alice", -1); // edad inválida
        when(userRepository.findById("1")).thenReturn(Mono.just(u));

        StepVerifier.create(userService.findById("1"))
                .expectNextMatches(user -> user.getName().equals("Fallback User")) // fallback
                .expectComplete()
                .verify();
    }

    @Test
    void testSaveSuccess() {
        User u = new User(null, "alice", 25);
        User saved = new User("1", "Alice", 25);

        when(userRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(userService.save(u))
                .expectNextMatches(user -> user.getName().equals("Alice"))
                .expectComplete()
                .verify();
    }

    @Test
    void testDeleteByIdSuccess() {
        User u = new User("1", "Alice", 25);
        when(userRepository.findById("1")).thenReturn(Mono.just(u));
        when(userRepository.deleteById("1")).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteById("1"))
                .expectComplete()
                .verify();
    }
}

