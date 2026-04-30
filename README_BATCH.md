# Programa Batch de carga inicial de base de datos

## ¿Qué es?

Es un programa Java Spring que se ejecuta automáticamente cuando inicia la aplicación y carga datos iniciales en la base de datos.

En este proyecto está implementado en:

```text
src/main/java/com/javeriana/vigiturno/batch/SeedDataRunner.java
```

## ¿Qué carga?

El batch crea información requerida para probar el sistema:

- Usuarios: administrador, coordinador y docentes.
- Zonas de vigilancia: Patio central, Cafetería, Cancha múltiple y Pasillo primaria.
- Turnos para el día actual y el día siguiente.
- Incidentes de ejemplo.
- Notificaciones.
- Una reasignación de turno.

## ¿Cuándo se ejecuta?

Se ejecuta al iniciar Spring Boot:

```bash
./mvnw spring-boot:run
```

En Windows:

```bash
mvnw.cmd spring-boot:run
```

## ¿Cómo evita duplicar datos?

Antes de insertar, revisa si ya existen usuarios o zonas:

```java
if (usuarioRepository.count() > 0 || zonaRepository.count() > 0) {
    return;
}
```

Así, si ya hay datos en la base de datos, el batch no vuelve a insertar la información.

## Cómo explicarlo en el video

Puedes decir:

> Para cumplir el entregable del programa Batch en Java Spring, implementamos una clase llamada SeedDataRunner dentro del paquete batch. Esta clase implementa CommandLineRunner, por lo que Spring Boot la ejecuta automáticamente al iniciar la aplicación. Su función es cargar la base de datos con información inicial requerida para probar el sistema: usuarios, zonas, turnos, incidentes, notificaciones y reasignaciones. Además, valida si la base de datos ya tiene información para evitar duplicados.
