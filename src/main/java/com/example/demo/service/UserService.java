package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Servicio reactivo de usuarios.
 *
 * Aquí se combinan ejemplos de operadores de Reactor (Mono/Flux) para
 * mostrar transformación de datos, combinación de streams, manejo de errores,
 * control de hilos, backpressure y delays.
 *
 * Cada metodo contiene comentarios detallados para servir como apuntes.
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // =============================================================
    // EJEMPLO 1: findAll() - map, filter, doOnNext, doOnComplete
    // =============================================================
    /**
     * Retorna todos los usuarios adultos (edad >= 18), transforma sus nombres
     * a mayúsculas y realiza logging en cada emisión.
     *
     * Flujo:
     *  - findAll() -> Flux<User>
     *  - filter() -> descarta menores de 18
     *  - map() -> transforma el nombre a mayúsculas
     *  - doOnNext() -> logging de cada usuario emitido
     *  - doOnComplete() -> logging al finalizar la emisión de todos los usuarios
     */
    public Flux<User> findAll() {
        return userRepository.findAll()
                .filter(u -> u.getAge() >= 18)
                .map(u -> {
                    u.setName(u.getName().toUpperCase());
                    return u;
                })
                .doOnNext(u -> System.out.println("User emitted: " + u.getName()))
                .doOnComplete(() -> System.out.println("All users emitted"));
    }

    // =============================================================
    // EJEMPLO 2: findById() - flatMap, map, onErrorResume, doOnSuccess
    // =============================================================
    /**
     * Busca un usuario por ID y aplica lógica reactiva con manejo de errores.
     *
     * Flujo:
     *  - findById() -> Mono<User>
     *  - flatMap() -> si edad < 0 genera error
     *  - map() -> limpia espacios del nombre
     *  - onErrorResume() -> captura errores y devuelve un "Fallback User"
     *  - doOnSuccess() -> logging al finalizar el flujo
     */
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
    // EJEMPLO 3: save() - filter, switchIfEmpty, flatMap, doFinally
    // =============================================================
    /**
     * Guarda un usuario asegurando que tenga nombre no vacío y capitalizando
     * la primera letra.
     *
     * Flujo:
     *  - Mono.just(user) -> crear flujo a partir del objeto
     *  - filter() -> descarta usuarios sin nombre
     *  - switchIfEmpty() -> genera error si filtro no pasa
     *  - map() -> capitaliza la primera letra
     *  - flatMap() -> delega a repository.save() (retorna Mono<User>)
     *  - doOnNext() -> logging al guardar
     *  - doFinally() -> logging con señal final (onComplete, onError, cancel)
     */
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
    // EJEMPLO 4: deleteById() - then, flatMap, onErrorResume
    // =============================================================
    /**
     * Elimina un usuario por ID.
     *
     * Flujo:
     *  - findById() -> Mono<User>
     *  - switchIfEmpty() -> genera error si no existe
     *  - flatMap() -> llama a deleteById
     *  - then() -> ignora resultado previo y retorna Mono<Void>
     *  - doOnTerminate() -> logging al terminar
     *  - onErrorResume() -> captura errores y retorna Mono.empty()
     */
    public Mono<Void> deleteById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found")))
                .flatMap(u -> userRepository.deleteById(id))
                .then()
                .doOnTerminate(() -> System.out.println("Delete terminated"))
                .onErrorResume(err -> {
                    System.out.println("Delete error: " + err.getMessage());
                    return Mono.empty();
                });
    }

    // =============================================================
    // EJEMPLO 5: combineFluxes() - zip, merge, concat
    // =============================================================
    /**
     * Combina múltiples Flux usando distintos operadores:
     *  - zip -> combina elementos en pares
     *  - merge -> combina de forma asincrónica
     *  - concat -> combina secuencialmente
     *
     * Útil para combinar streams distintos en un solo flujo.
     */
    public Flux<String> combineFluxes() {
        Flux<String> names = Flux.just("Ana", "Luis", "Marta");
        Flux<Integer> ages = Flux.just(25, 30, 22);

        Flux<String> zipped = Flux.zip(names, ages, (n, a) -> n + " is " + a);
        Flux<String> merged = names.mergeWith(Flux.just("Carlos", "Lucia"));
        Flux<String> concatenated = names.concatWith(Flux.just("FinalName"));

        return zipped.concatWith(merged).concatWith(concatenated)
                .doOnNext(System.out::println)
                .doOnComplete(() -> System.out.println("All combined"));
    }

    // =============================================================
    // EJEMPLO 6: errorHandlingExample() - onErrorContinue
    // =============================================================
    /**
     * Muestra cómo manejar errores en medio de un flujo sin detenerlo.
     * onErrorContinue permite saltar elementos que causan error.
     */
    public Flux<Integer> errorHandlingExample() {
        return Flux.just("1", "2", "x", "4")
                .map(Integer::parseInt)
                .onErrorContinue((e, obj) -> System.out.println("Error parsing: " + obj))
                .doOnComplete(() -> System.out.println("Parsing complete"));
    }

    // =============================================================
    // EJEMPLO 7: backpressureExample() - limitRate
    // =============================================================
    /**
     * Demuestra control de flujo (backpressure) usando limitRate.
     * Se procesan 50 elementos por vez, útil en streams grandes.
     */
    public Flux<Integer> backpressureExample() {
        return Flux.range(1, 1000)
                .log()
                .limitRate(50)
                .doOnNext(i -> System.out.println("Processing: " + i));
    }

    // =============================================================
    // EJEMPLO 8: schedulerExample() - publishOn / subscribeOn
    // =============================================================
    /**
     * Controla en qué threads se ejecuta el flujo:
     *  - publishOn -> cambia el thread a mitad del flujo
     *  - subscribeOn -> define thread inicial
     *
     * Útil para operaciones de IO o paralelización.
     */
    public Flux<String> schedulerExample() {
        return Flux.just("alpha", "beta", "gamma")
                .publishOn(Schedulers.parallel())
                .map(s -> {
                    System.out.println(Thread.currentThread().getName() + " processing " + s);
                    return s.toUpperCase();
                })
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> System.out.println("Scheduler example complete"));
    }

    // =============================================================
    // EJEMPLO 9: intervalExample() - delay, Flux.interval
    // =============================================================
    /**
     * Genera un flujo con retraso usando Flux.interval.
     * take(n) limita el número de emisiones.
     * doOnNext -> logging de cada tick
     * doOnComplete -> logging al finalizar
     */
    public Flux<Long> intervalExample() {
        return Flux.interval(Duration.ofSeconds(1))
                .take(5)
                .doOnNext(i -> System.out.println("Tick: " + i))
                .doOnComplete(() -> System.out.println("Interval complete"));
    }
}
