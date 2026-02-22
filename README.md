# Proyecto Reactivo con Spring Boot – Glosario (Completo)

## 1. Introducción

Este proyecto utiliza **Spring Boot WebFlux** para construir aplicaciones **reactivas**, es decir sistemas que procesan datos de manera asincrónica y no bloqueante. La programación reactiva está diseñada para aplicaciones que necesitan manejar **muchas conexiones concurrentes**, procesar **streams de datos continuos** (sensores, logs, eventos) o ejecutar operaciones de I/O intensivas sin saturar el servidor ni su pool de hilos.

Los conceptos de base son **`Mono`** y **`Flux`** (tipos reactivas), una colección de **operadores** para transformar y combinar flujos, **mecanismos de control de hilos** (`Schedulers`) y patrones de resiliencia y backpressure. Además del servidor reactivo, se considera la integración con clientes HTTP reactivas, bases de datos reactivos y sistemas de mensajería reactivos (Kafka).

---

## 2. Conceptos básicos de programación reactiva

- **Flux**: representa un flujo que puede emitir **0..N elementos** a lo largo del tiempo. Es adecuado para colecciones reactivas, streams de eventos, respuestas paginadas y emisiones periódicas. Un `Flux` puede completar, puede emitir errores, y puede ser infinito si no se aplica un límite.

- **Mono**: representa un flujo que puede emitir **0..1 elemento**. Ideal para operaciones unitarias: obtener un registro por ID, crear un recurso y recibir su representación, o realizar una llamada HTTP que devuelve un único objeto.

- **Operadores**: funciones que transforman, combinan, filtran o reaccionan a los elementos del flujo sin bloquear. Se componen de forma declarativa para construir pipelines. Los operadores no ejecutan hasta que exista una suscripción (modelo lazy).

- **Publisher / Subscriber / Subscription**: términos de la especificación Reactive Streams. `Publisher` emite (ej. `Flux`), `Subscriber` consume, y `Subscription` controla la demanda (cuántos elementos solicitar), lo que permite controlar backpressure.

---

## 3. Operadores comunes

- **map()**: transforma cada elemento aplicando una función puramente transformadora. Mantiene el 1:1 de elementos. Uso: limpieza de datos, proyección, conversiones sencillas.

- **flatMap()**: transforma cada elemento en un `Publisher` (Mono o Flux) y aplana el resultado en un solo flujo. Uso: cuando cada elemento requiere una operación asíncrona que devuelve un flujo (p. ej. llamada a API / BD por elemento). Permite concurrencia por defecto.

- **filter()**: descarta elementos que no cumplan la condición. Útil para validar o filtrar datos en pipelines.

- **then()**: ignora el valor previo y retorna un `Mono` que representa la finalización del flujo anterior o un flujo alternativo. Uso frecuente en operaciones donde solo importa la ejecución (p. ej. eliminar un recurso y devolver `Mono<Void>`).

- **switchIfEmpty()**: si el flujo fuente no emite (está vacío), devuelve un flujo alternativo. Útil para fallback o para ofrecer un valor por defecto cuando no hay resultados.

- **doOnNext(), doOnSuccess(), doOnComplete(), doFinally()**: hooks para efectos secundarios y logging sin alterar los datos. `doFinally()` se ejecuta siempre, independientemente de la señal final (onComplete, onError, cancel).

---

## 4. Operadores avanzados

- **zip()**: combina elementos de múltiples flujos en tuplas (o transforma con combinador) de forma sincrónica por índice: el resultado se produce cuando todos los flujos han producido el elemento correspondiente. El tamaño del resultado está limitado por el flujo más corto.

- **merge()**: mezcla flujos de forma concurrente y emite cada elemento tan pronto como llega. No garantiza orden entre flujos.

- **concat()**: concatena flujos secuencialmente. Termina un flujo y luego comienza el siguiente. Garantiza orden estricto entre flujos.

- **combineLatest()**: emite cada vez que cualquiera de los flujos emite, combinando los últimos valores conocidos de cada flujo. Útil cuando varias fuentes representan estados independientes y quieres resultados combinados reactivos.

- **withLatestFrom()**: emite solo cuando el flujo principal emite, incorporando el último valor de los flujos secundarios. Útil cuando existe un trigger principal (p. ej. pulsación de usuario) y se quiere enriquecer con el último estado.

- **amb() / firstWithSignal()**: toma solo el flujo que emite primero y descarta los demás. Útil para redundancia o elegir la respuesta más rápida.

- **reduce()**: acumula todos los elementos y produce un único resultado al finalizar (similar a `collect`), emite solo al final.

- **scan()**: acumula y emite el estado intermedio tras cada elemento. Útil para contadores acumulativos o visualización de progreso.

- **buffer() / window()**: agrupa elementos en listas (buffer) o ventanas (window) por tamaño o tiempo. Aplicable para procesar lotes o ventanas temporales.

- **delayElements() / delaySequence()**: introduce retardos entre emisiones (simulación de tiempo o throttle).

- **take() / takeUntil() / takeWhile()**: controlan cuánto emitir: por número o por condición de corte.

- **skip() / skipUntil()**: ignoran elementos iniciales hasta cierta condición.

- **distinct() / distinctUntilChanged()**: eliminan duplicados globales o consecutivos.

- **onErrorResume() / onErrorReturn() / onErrorContinue()**: manejo de errores: sustitución por flujo alternativo, valor fijo o continúa con los siguientes elementos ignorando el que falló.

---

## 5. Backpressure y control de flujo

- **Principio**: backpressure permite al consumidor controlar la velocidad del productor solicitando `n` elementos a la vez. Sin control, flujos rápidos pueden saturar memoria o hilos.

- **limitRate(n)**: solicita `n` elementos en cada petición de demanda; empuja la velocidad de producción a un ritmo controlado.

- **onBackpressureBuffer(size)**: acumula elementos en buffer cuando la demanda no da abasto (riesgo de memoria si se ignora).

- **onBackpressureDrop() / onBackpressureLatest()**: estrategias de descarte (descartar elementos excedentes o mantener solo el último).

- **publishOn(Scheduler)**: cambia el hilo de ejecución a partir de ese punto en el pipeline. Todos los operadores subsiguientes se ejecutan en ese `Scheduler`.

- **subscribeOn(Scheduler)**: define el hilo donde inicia la suscripción y la generación desde la fuente. Afecta al inicio del flujo.

- **request(n)**: en escenarios avanzados, controlar manualmente la petición de elementos a la `Subscription` para implementar políticas personalizadas.

---

## 6. Schedulers en Reactor

- **immediate()**: ejecuta en el mismo hilo de suscripción. Útil para pruebas o operaciones triviales; peligro de bloqueo si se ejecuta trabajo pesado.

- **single()**: un hilo único compartido. Útil para operaciones que requieren secuencialidad absoluta.

- **parallel()**: pool optimizado para CPU-bound, tamaño por defecto = número de CPU. Ideal para transformaciones pesadas (`map`) y procesamiento concurrente.

- **boundedElastic()**: pool elástico limitado, diseñado para operaciones de I/O bloqueante (acceso a archivos, drivers bloqueantes). Expande hilos según demanda pero con límites para no agotar la JVM.

- **newSingle("name")**: hilo dedicado con nombre, para tareas que deben aislarse.

- **fromExecutor(Executor)**: usar un `ExecutorService` personalizado, para control fino de tamaño y políticas de hilos.

---

## 7. Streams de tiempo y eventos

- **Flux.interval(Duration)**: genera secuencia de ticks (0,1,2...) con la periodicidad indicada. Muy útil para simulaciones, polling o generación de eventos periódicos.

- **take(n)**: limitar emisiones en flujos potencialmente infinitos.

- **delayElements(Duration)**: retardar emisiones para modelar latencias.

- **Server-Sent Events (SSE)**: protocolo unidireccional HTTP para que el servidor envíe eventos al cliente continuamente. En WebFlux se devuelve un `Flux` con `MediaType.TEXT_EVENT_STREAM_VALUE`. Fácil de implementar y consumir en navegadores, con reconexión automática simple.

---

## 8. Integración con servicios externos: WebClient

- Cliente HTTP reactivo (`WebClient`) reemplaza RestTemplate para pipelines no bloqueantes.
- Filtros (`ExchangeFilterFunction`) permiten interceptar peticiones/respuestas para logging, cabeceras, tracing o métricas.
- Soporta manejo de errores, timeout, retry/backoff y fallback.
- Combinable con flujos internos usando `flatMap`, `zip`, `merge`.

---

## 9. Testing reactivo

- **StepVerifier**: valida emisiones esperadas de Mono/Flux, errores y completado.
- **WebTestClient**: prueba controladores WebFlux sin servidor completo.
- **VirtualTimeScheduler**: simula tiempo para tests de delay/interval sin retrasos reales.
- Logging con `flux.log()` y checkpoints para depuración de pipelines complejos.

---

## 10. Buenas prácticas

- No bloquear hilos.
- Separar operaciones CPU vs IO.
- Manejar errores con operadores reactivos.
- Controlar backpressure.
- Logging y tracing contextual.
- Pruebas deterministas con virtual time.

---

## 11. SSE y WebSockets

- **SSE**: unidireccional, `Flux<T>` → `text/event-stream`.
- **WebSockets**: bidireccional, usando `WebSocketHandler`, `HandlerMapping` y `WebSocketHandlerAdapter`.
- Autenticación y control de flujo de mensajes son esenciales en producción.

---

## 12. Bases de datos reactivas

- **R2DBC**: acceso SQL no bloqueante.
- **ReactiveMongoRepository**: acceso MongoDB reactivo.
- Integración con Mono/Flux y transacciones reactivas.

---

## 13. Kafka reactivo (Reactor Kafka)

- Productores y consumidores reactivos (`KafkaSender`, `KafkaReceiver`) integrados con `Flux`.
- Control de backpressure, retries, offsets y DLQ.
- Integrable con WebFlux, SSE y WebSocket para flujos de eventos.
- Ideal para arquitecturas 100% non-blocking.

---

## 14. Patrones y resiliencia

- `retryWhen` con backoff.
- `timeout` y `switchIfEmpty` para fallback.
- `flatMapSequential` vs `concatMap` para concurrencia + orden.
- `onErrorContinue` para procesar lotes ignorando fallos puntuales.

---

## 15. Integración completa

- Pipelines combinando IO (WebClient/DB) y CPU (procesamiento) usando Schedulers.
- Microservicios reactivos end-to-end: Controller → Service → Repository / External Service → Kafka.
- Observabilidad con metrics, tracing y logging.

---

## 16. Checklist producción

- Versiones compatibles.
- Monitoreo y alertas.
- Circuit Breakers / Bulkheads.
- Configuración de timeouts.
- Pruebas de carga y control de backpressure.
- Pools adecuados para boundedElastic y parallel.

---

## 17. Glosario rápido

| Concepto | Descripción |
|----------|-------------|
| Mono | Flujo de 0 o 1 elemento |
| Flux | Flujo de 0 o N elementos |
| map | Transformación pura de elementos |
| flatMap | Transformación que aplanan flujos internos |
| concatMap | Procesamiento secuencial por elemento |
| flatMapSequential | Concurrencia + preservación de orden |
| zip | Sincronización par a par |
| merge | Mezcla concurrente de flujos |
| combineLatest | Emite cuando cualquiera emite, combinando últimos valores |
| limitRate | Control de demanda (backpressure) |
| onBackpressureBuffer / Drop / Latest | Estrategias para exceso de producción |
| subscribeOn | Define hilo inicial |
| publishOn | Cambia hilo a mitad del pipeline |
| boundedElastic | Scheduler para I/O bloqueante |
| parallel | Scheduler para operaciones CPU-bound |
| WebClient | Cliente HTTP reactivo |
| SSE | Server Sent Events, streaming unidireccional |
| WebSocketHandler | Handler reactivo para WebSocket |
| HandlerMapping | Mapeo de URL a handlers |
| WebSocketHandlerAdapter | Adaptador WebFlux para WebSocket |
| R2DBC | Driver/reactive spec para bases SQL |
| ReactiveMongoRepository | Repositorios reactivos para MongoDB |
| Reactor Kafka | Cliente Kafka reactivo para producir/consumir |

---

## 18. Referencias tecnológicas

- :contentReference[oaicite:0]{index=0}
- :contentReference[oaicite:1]{index=1}
- :contentReference[oaicite:2]{index=2}
- :contentReference[oaicite:3]{index=3}
- :contentReference[oaicite:4]{index=4}
- :contentReference[oaicite:5]{index=5}
- :contentReference[oaicite:6]{index=6}  