package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Tag(name = "User Controller", description = "Reactive CRUD operations and reactive behavior demonstrations")
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // =============================================================
    // BASIC CRUD ENDPOINTS
    // =============================================================

    @Operation(summary = "Get all users", description = "Returns all users as a reactive Flux stream.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "500", description = "Server error", content = @Content)
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }

    @Operation(summary = "Get user by ID", description = "Returns a single user based on provided ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> getUserById(
            @Parameter(description = "User ID to search", example = "1") @PathVariable String id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Create new user", description = "Creates a new user in the reactive repository.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User created",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content)
    })

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> createUser(
            @RequestBody(description = "User JSON to create",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(value = "{\"name\":\"Ana\",\"age\":25}")))
            @org.springframework.web.bind.annotation.RequestBody User user) {

        return userService.save(user)
                .map(ResponseEntity::ok)
                .onErrorResume(err -> {
                    System.out.println("Error creating user: " + err.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                });
    }

    @Operation(summary = "Delete user by ID", description = "Deletes a user from the repository by ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping(value = "/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(
            @Parameter(description = "User ID to delete", example = "1") @PathVariable String id) {
        return userService.deleteById(id)
                .thenReturn(ResponseEntity.ok().<Void>build())
                .onErrorResume(err -> {
                    System.out.println("Delete error: " + err.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    // =============================================================
    // ADVANCED REACTIVE EXAMPLES
    // =============================================================

    @Operation(summary = "Combine reactive streams", description = "Demonstrates the use of zip, merge, and concat operators.")
    @ApiResponse(responseCode = "200", description = "Streams combined successfully",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class)))
    @GetMapping(value = "/combine", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> combineFluxes() {
        return userService.combineFluxes();
    }

    @Operation(summary = "Error handling example", description = "Shows how onErrorContinue allows flow to continue after an error.")
    @ApiResponse(responseCode = "200", description = "Flux executed with error handling",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Integer.class)))
    @GetMapping(value = "/errors", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Integer> errorHandlingExample() {
        return userService.errorHandlingExample();
    }
    @Operation(summary = "Backpressure example", description = "Demonstrates how limitRate controls demand from a Flux.")
    @ApiResponse(responseCode = "200", description = "Flux with backpressure applied",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Integer.class)))
    @GetMapping(value = "/backpressure", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Integer> backpressureExample() {
        return userService.backpressureExample();
    }

    @Operation(summary = "Schedulers example", description = "Demonstrates switching threads using publishOn and subscribeOn.")
    @ApiResponse(responseCode = "200", description = "Flux executed on different schedulers",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = String.class)))
    @GetMapping(value = "/schedulers", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> schedulerExample() {
        return userService.schedulerExample();
    }

    @Operation(summary = "Interval stream", description = "Demonstrates a timed Flux using interval and take operators.")
    @ApiResponse(responseCode = "200", description = "Flux emitting values periodically",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Long.class)))
    @GetMapping(value = "/interval", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<Long> intervalExample() {
        return userService.intervalExample();
    }

    // =============================================================
    // EXTRA EXAMPLE: VALIDATION ENDPOINT
    // =============================================================
    @Operation(summary = "Validate and enrich user", description = "Simulates validation of input user data and enrichment process.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Validated and enriched user returned",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data", content = @Content)
    })
    @PostMapping(value = "/validate", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<User>> validateAndEnrich(
            @RequestBody(description = "User object to validate",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = User.class),
                            examples = @ExampleObject(value = "{\"name\":\"Carlos\",\"age\":28}")))
            @org.springframework.web.bind.annotation.RequestBody User user) {

        return userService.save(user)
                .map(u -> {
                    u.setName(u.getName() + " (validated)");
                    return ResponseEntity.ok(u);
                })
                .onErrorResume(err -> Mono.just(ResponseEntity.badRequest().build()));
    }
}