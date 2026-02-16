package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

// Servicio que encapsula logica de negocio usando tipos reactivos Mono/Flux
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =============================================================
    // EXAMPLE 1: Basic map / flatMap / filter / then
    // =============================================================
    public Flux<User> findAll() {
        return userRepository.findAll()
                .filter(u -> u.getAge() >= 18) // only adults
                .map(u -> {
                    u.setName(u.getName().toUpperCase());
                    return u;
                })
                .doOnNext(u -> System.out.println("User emitted: " + u.getName()))
                .doOnComplete(() -> System.out.println("All users emitted"));
    }

    // =============================================================
    // EXAMPLE 2: Mono chain with flatMap, map and error handling
    // =============================================================
    public Mono<User> findById(String id) {
        return userRepository.findById(id)
                .flatMap(u -> {
                    if (u.getAge() < 0) {
                        return Mono.error(new IllegalArgumentException("Invalid age"));
                    }
                    return Mono.just(u);
                })
                .map(u -> {
                    u.setName(u.getName().trim());
                    return u;
                })
                .onErrorResume(err -> {
                    System.out.println("Error found: " + err.getMessage());
                    return Mono.just(new User("0", "Fallback User", 0));
                })
                .doOnSuccess(u -> System.out.println("Finished processing user id=" + id));
    }

    // =============================================================
    // EXAMPLE 3: Save operation with switchIfEmpty and doFinally
    // =============================================================
    public Mono<User> save(User user) {
        return Mono.just(user)
                .filter(u -> u.getName() != null && !u.getName().isBlank())
                .switchIfEmpty(Mono.error(new RuntimeException("Name required")))
                .map(u -> {
                    u.setName(u.getName().substring(0, 1).toUpperCase() + u.getName().substring(1));
                    return u;
                })
                .flatMap(u -> userRepository.save(u))
                .doOnNext(u -> System.out.println("Saved user: " + u.getName()))
                .doFinally(signal -> System.out.println("Save finished with signal: " + signal));
    }

    // =============================================================
    // EXAMPLE 4: deleteById() with then and error handling
    // =============================================================
    public Mono<Void> deleteById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(u -> userRepository.deleteById(id))
                .then() // ignore previous result and complete
                .doOnTerminate(() -> System.out.println("Delete terminated"))
                .onErrorResume(err -> {
                    System.out.println("Delete error: " + err.getMessage());
                    return Mono.empty();
                });
    }

    // =============================================================
    // EXAMPLE 5: Combining Flux streams (zip / merge / concat)
    // =============================================================
    public Flux<String> combineFluxes() {
        Flux<String> names = Flux.just("Ana", "Luis", "Marta");
        Flux<Integer> ages = Flux.just(25, 30, 22);

        // zip combines pairwise
        Flux<String> zipped = Flux.zip(names, ages, (n, a) -> n + " is " + a);

        // merge combines asynchronously
        Flux<String> merged = names.mergeWith(Flux.just("Carlos", "Lucia"));

        // concat combines sequentially
        Flux<String> concatenated = names.concatWith(Flux.just("FinalName"));

        return zipped.concatWith(merged).concatWith(concatenated)
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("All combined"));
    }

    // =============================================================
    // EXAMPLE 6: Error handling operators
    // =============================================================
    public Flux<Integer> errorHandlingExample() {
        return Flux.just("1", "2", "x", "4")
                .map(Integer::parseInt)
                .onErrorContinue((e, obj) -> System.out.println("Error parsing: " + obj)) // skip invalid elements
                .doOnComplete(() -> System.out.println("Parsing complete"));
    }

    // =============================================================
    // EXAMPLE 7: Backpressure control
    // =============================================================
    public Flux<Integer> backpressureExample() {
        return Flux.range(1, 1000)
                .log()
                .limitRate(50) // request 50 elements at a time
                .doOnNext(i -> System.out.println("Processing: " + i));
    }

    // =============================================================
    // EXAMPLE 8: Schedulers - controlling threads
    // =============================================================
    public Flux<String> schedulerExample() {
        return Flux.just("alpha", "beta", "gamma")
                .publishOn(Schedulers.parallel()) // switch to parallel thread pool
                .map(s -> {
                    System.out.println(Thread.currentThread().getName() + " processing " + s);
                    return s.toUpperCase();
                })
                .subscribeOn(Schedulers.boundedElastic()) // start in elastic thread pool
                .doOnComplete(() -> System.out.println("Scheduler example complete"));
    }

    // =============================================================
    // EXAMPLE 9: Delay and infinite stream (Flux.interval)
    // =============================================================
    public Flux<Long> intervalExample() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5) // take first 5 emissions
                .doOnNext(i -> System.out.println("Tick: " + i))
                .doOnComplete(() -> System.out.println("Interval complete"));
    }
}