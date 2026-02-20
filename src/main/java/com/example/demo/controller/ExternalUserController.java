package com.example.demo.controller;

import com.example.demo.model.ExternalUser;
import com.example.demo.service.ExternalUserService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/external-users")
public class ExternalUserController {

    private final ExternalUserService externalUserService;

    public ExternalUserController(ExternalUserService externalUserService) {
        this.externalUserService = externalUserService;
    }

    @GetMapping
    public Flux<ExternalUser> getAll() {
        return externalUserService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Mono<ExternalUser> getById(@PathVariable int id) {
        return externalUserService.getUserById(id);
    }

    @PostMapping
    public Mono<ExternalUser> create(@RequestBody ExternalUser user) {
        return externalUserService.createUser(user);
    }
}
