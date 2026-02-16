# Spring WebFlux - Ejemplo Reactivo

Proyecto de ejemplo para aprender WebFlux + Reactor.

## Requisitos
- JDK 17+
- Maven
- MongoDB (opcional, si usas la configuracion por defecto)

## Ejecutar
1. Inicia MongoDB local en puerto 27017 (o ajusta application.yml)
2. Ejecuta:
   ```bash
   mvn spring-boot:run 
3. Probar endpoints: curl http://localhost:8080/hello
   curl -s http://localhost:8080/users | jq

Conceptos a estudiar junto al codigo
Diferencia Mono vs Flux

Operadores: map, flatMap, filter, zip, merge

Manejo de errores: onErrorResume, onErrorReturn

Backpressure

Pruebas con reactor-test

---

## Comentarios didacticos (puntos clave dentro del codigo)

- Todos los repositorios y servicios devuelven `Mono` o `Flux`: esto permite encadenar transformaciones
  sin bloquear hilos.
- `Flux` es una secuencia: ideal para listar elementos. `Mono` es 0..1: ideal para operaciones por id o resultados unicos.
- Las llamadas a BD reactiva NO ejecutan nada hasta que haya un `subscribe` por parte del framework o de pruebas.
- En controladores WebFlux, Spring se encarga de suscribirse y gestionar el ciclo de vida de la respuesta HTTP.
- Evitar en la medida de lo posible `block()` en codigo de produccion: `block()` rompe el modelo no bloqueante.

---

## Siguientes pasos (sugeridos)

1. Ejecutar el proyecto y probar los endpoints con `curl` o Postman.
2. Añadir ejemplos de operadores en el servicio: por ejemplo usar `flatMap` para validar o enriquecer datos antes de guardar.
3. Escribir tests unitarios con `reactor-test` para verificar flujos asinc.
4. Reemplazar Mongo por R2DBC 

