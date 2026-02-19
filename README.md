# Proyecto Reactivo con Spring Boot – Glosario

## 1. Introducción

Este proyecto utiliza **Spring Boot WebFlux** para construir aplicaciones **reactivas**, que permiten procesar datos de manera asincrónica y no bloqueante.  
La programación reactiva es útil para aplicaciones que manejan **muchas conexiones concurrentes**, streams de datos continuos, o tareas de IO intensivas sin saturar el servidor.  
Los conceptos clave incluyen **Mono y Flux**, **operadores de Reactor**, **manejo de hilos** y técnicas avanzadas de flujo de datos.

---

## 2. Conceptos básicos de programación reactiva

- **Flux**: representa un flujo que puede emitir **cero, uno o muchos elementos** de manera asincrónica. Es ideal para listas, streams de eventos o colecciones de datos que llegan progresivamente.
- **Mono**: representa un flujo que puede emitir **cero o un elemento**. Es útil para operaciones que devuelven un único resultado, como obtener un registro por ID o una respuesta de un servicio externo.
- **Operadores**: métodos que permiten **transformar, filtrar, combinar o reaccionar** a los elementos de un flujo sin bloquear el hilo. Permiten componer lógica compleja de manera declarativa y funcional.

---

## 3. Operadores comunes

- **map()**: transforma cada elemento del flujo de forma independiente, aplicando una función a cada valor. No altera la cantidad de elementos, solo los transforma.
- **flatMap()**: transforma cada elemento a un **nuevo flujo** (Mono o Flux) y luego los aplana en un solo flujo continuo. Es clave cuando una operación devuelve un flujo interno.
- **filter()**: filtra elementos según una condición, descartando los que no cumplen el criterio.
- **then()**: ignora el valor de los elementos previos y devuelve un Mono vacío o el resultado de un flujo alternativo.
- **switchIfEmpty()**: define un flujo alternativo si el flujo original está vacío, útil para fallback o valores por defecto.
- **doOnNext() / doOnSuccess() / doOnComplete() / doFinally()**: permiten ejecutar efectos secundarios o logging en distintos puntos del flujo sin alterar los datos.

---

## 4. Operadores avanzados

- **zip()**: combina elementos de dos o más flujos de manera **paralela**, tomando un elemento de cada flujo y uniéndolos en un solo valor.
- **merge()**: combina múltiples flujos de forma **asincrónica y concurrente**, sin esperar que uno termine antes que otro.
- **concat()**: combina flujos de manera **secuencial**, esperando que un flujo termine antes de iniciar el siguiente.
- **onErrorResume() / onErrorReturn() / onErrorContinue()**: operadores para **manejo de errores** dentro del flujo, permitiendo capturar excepciones y continuar la ejecución o proporcionar valores alternativos.
- **buffer() / window()**: agrupa elementos en **listas o ventanas temporales**, útil para procesar lotes o ventanas de tiempo.
- **delayElements() / delaySequence()**: introduce **retrazos entre emisiones**, permitiendo simular tiempos de espera o diferir la emisión de eventos.
- **take() / takeUntil() / takeWhile()**: limita la cantidad de elementos emitidos según un número, condición o predicado.
- **skip() / skipUntil()**: ignora elementos al inicio del flujo hasta cumplir una condición o cantidad.
- **distinct() / distinctUntilChanged()**: elimina elementos duplicados, ya sea en todo el flujo o solo consecutivos.

---

## 5. Backpressure y control de flujo

- **limitRate(n)**: controla la **cantidad de elementos solicitados a la vez**, evitando saturar al consumidor o al sistema. Es esencial en flujos grandes o infinitos.
- **publishOn(Scheduler)**: cambia el hilo de ejecución a partir de un punto específico del flujo. Todos los operadores que siguen se ejecutan en el Scheduler indicado.
- **subscribeOn(Scheduler)**: define el hilo en el que se inicia la suscripción y la generación de datos de la fuente. Afecta a todo el flujo desde el inicio.

---

## 6. Schedulers en Reactor

- **immediate()**: ejecuta el flujo en el mismo hilo de suscripción, sin crear hilos adicionales. Adecuado para operaciones muy ligeras o testing.
- **single()**: crea un **único hilo compartido** para flujos que deben ejecutarse de manera secuencial y no paralela.
- **parallel()**: pool de hilos optimizado para **procesamiento intensivo de CPU**, con tamaño igual al número de núcleos disponibles. Ideal para map/filter pesados.
- **boundedElastic()**: pool elástico limitado, ideal para operaciones de **IO bloqueante**, como llamadas a bases de datos o servicios externos. Ajusta el número de hilos según demanda pero mantiene un límite seguro.
- **newSingle()**: crea un hilo dedicado exclusivo para un flujo, útil cuando se necesita secuencialidad absoluta.
- **fromExecutor(Executor)**: permite usar un `ExecutorService` personalizado como Scheduler, dando control total sobre tamaño de pool, prioridad y manejo de hilos.

---

## 7. Streams de tiempo y eventos

- **Flux.interval(Duration)**: genera un flujo de números secuenciales cada intervalo de tiempo definido, simulando **ticks o eventos periódicos**.
- **take(n)**: limita las emisiones para flujos que podrían ser infinitos, útil para tests o demos.
- **Server-Sent Events (SSE)**: mecanismo para enviar un flujo continuo de datos a clientes HTTP de manera reactiva, muy útil en aplicaciones en tiempo real.

---

## 8. Integración con servicios y bases de datos

- **WebClient**: cliente HTTP **reactivo**, capaz de consumir APIs externas sin bloquear hilos.
- **R2DBC**: driver reactivo para bases de datos SQL, que permite devolver Mono/Flux directamente y trabajar de manera no bloqueante.
- **Repositories reactivos**: permiten que los métodos del repositorio devuelvan directamente Mono o Flux, integrando la base de datos en el flujo reactivo.

---

## 9. Testing reactivo

- **StepVerifier**: herramienta para verificar que Mono o Flux **emite los valores esperados**, se completa correctamente y maneja errores como se espera.
- Permite testear operadores, manejo de errores, límites de flujo y backpressure de manera **no bloqueante**, asegurando el comportamiento reactivo.

---

## 10. Buenas prácticas

- **Nunca bloquear hilos** en flujos reactivos (evitar llamadas sincrónicas o Thread.sleep()).
- **Separar operaciones de CPU y IO** usando los Schedulers adecuados.
- **Manejo de errores** con operadores reactivos (`onErrorResume`, `onErrorContinue`) en lugar de try/catch tradicionales.
- **Evitar flujos infinitos sin límites o control de backpressure**, para no saturar la aplicación o la JVM.
- **Logging y debugging**: usar `doOnNext`, `doOnComplete`, `doFinally` para inspeccionar el flujo y detectar problemas de concurrencia.

---


## 11. Flujo de tiempo y retrasos

- `Flux.interval(Duration.ofSeconds(1))`: genera emisiones periódicas.
- `take(n)`: limita el número de emisiones.
- `doOnNext`: permite logging de cada elemento emitido.
- `doOnComplete`: permite logging al finalizar.

---

## 12. WebSockets en Spring WebFlux

### **Componentes clave**
1. **WebSocketHandler**
    - Define la lógica de manejo de mensajes entrantes y salientes.
    - Ejemplo: transmitir temperaturas cada segundo.
2. **HandlerMapping**
    - Asocia URLs de WebSocket con su handler.
    - Ejemplo: `/ws/temperature` → `TemperatureWebSocketHandler`.
3. **WebSocketHandlerAdapter**
    - Permite a Spring WebFlux aceptar y procesar conexiones WebSocket.
    - Traduce el handshake a un `WebSocketSession` reactivo.

### **Flujo completo**
- Netty recibe el handshake WebSocket.
- WebSocketHandlerAdapter traduce la conexión.
- HandlerMapping encuentra el handler correspondiente.
- WebSocketHandler maneja la comunicación bidireccional usando `Flux` y `Mono`.

---


## 13. Glosario rápido

| Concepto | Descripción |
|----------|------------|
| Mono | Flujo de 0 o 1 elemento |
| Flux | Flujo de 0 o N elementos |
| map | Transformación de elementos |
| flatMap | Transformación y aplanado de flujos internos |
| then | Ignora valor previo, devuelve Mono de finalización |
| zip | Combina flujos par a par |
| merge | Combina flujos concurrentemente |
| concat | Combina flujos secuencialmente |
| onErrorResume | Reemplaza un error por un valor alternativo |
| onErrorContinue | Ignora un error y continúa |
| switchIfEmpty | Flujo alternativo si no hay elementos |
| limitRate | Control de backpressure |
| subscribeOn | Define hilo inicial del flujo |
| publishOn | Cambia hilo a mitad del flujo |
| boundedElastic | Scheduler para I/O bloqueante |
| parallel | Scheduler para operaciones CPU-bound |
| WebSocketHandler | Define lógica de WebSocket |
| HandlerMapping | Asocia URL con handler |
| WebSocketHandlerAdapter | Adaptador para manejar WebSocket con Netty |

---
