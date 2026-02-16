package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /users -> devuelve un Flux<User>
    @GetMapping
    public Flux<User> getAll() {
        return userService.findAll();
    }

    // GET /users/{id} -> devuelve Mono<ResponseEntity<User>>
    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getById(@PathVariable String id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(user))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // POST /users -> crea usuario, recibe JSON y devuelve Mono<User>
    @PostMapping
    public Mono<User> create(@RequestBody User user) {
        return userService.save(user);
    }

    // DELETE /users/{id}
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> delete(@PathVariable String id) {
        return userService.findById(id)
                .flatMap(u -> userService.deleteById(id).thenReturn(ResponseEntity.ok().<Void>build()))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
