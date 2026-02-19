package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserControllerTest {

    private UserService userService;
    private UserController userController;
    private WebTestClient webClient;

    @BeforeEach
    void setup() {
        userService = Mockito.mock(UserService.class);
        userController = new UserController(userService);
        // Creamos un cliente web simulado apuntando al controlador
        webClient = WebTestClient.bindToController(userController).build();
    }

    @Test
    void testGetAllUsers() {
        User u1 = new User("1", "Alice", 25);
        when(userService.findAll()).thenReturn(Flux.just(u1));

        webClient.get().uri("/users")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(User.class)
                .hasSize(1)
                .contains(u1);
    }

    @Test
    void testGetUserById() {
        User u = new User("1", "Alice", 25);
        when(userService.findById("1")).thenReturn(Mono.just(u));

        webClient.get().uri("/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(u);
    }

    @Test
    void testSaveUser() {
        User u = new User(null, "Alice", 25);
        User saved = new User("1", "Alice", 25);
        when(userService.save(any(User.class))).thenReturn(Mono.just(saved));

        webClient.post().uri("/users")
                .bodyValue(u)
                .exchange()
                .expectStatus().isOk()
                .expectBody(User.class)
                .isEqualTo(saved);
    }

    @Test
    void testDeleteUser() {
        when(userService.deleteById("1")).thenReturn(Mono.empty());

        webClient.delete().uri("/users/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();
    }
}

