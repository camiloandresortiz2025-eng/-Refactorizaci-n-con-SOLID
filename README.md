# Refactorización con SOLID — Tienda de Peceras

Taller de refactorización de la clase `TiendaPecerasLegacy` aplicando los cinco principios SOLID.

**Guía de Aprendizaje Nº 04 — Principios SOLID en el Desarrollo de Software**

| | |
|---|---|
| Programa | Análisis y Desarrollo de Software (ADSO) — 228118 |
| Competencia | 220501098 — Evaluar requisitos del software de acuerdo con técnicas de análisis y estándares de la industria |
| Resultado de aprendizaje | 593146-01 — Incorporar actividades de aseguramiento de la calidad del software |
| Actividades | 3.1 (reflexión inicial) y 3.4 (taller práctico de refactorización) |
| Centro | Centro de Gestión de Mercados, Logística y Tecnologías de la Información |

## Contenido del repositorio

| Ruta | Descripción |
|---|---|
| [`Taller_SOLID_TiendaPeceras.md`](Taller_SOLID_TiendaPeceras.md) | Documento del taller: respuestas de la reflexión inicial y diagnóstico completo de las violaciones SOLID con su corrección |
| `src/` | Código refactorizado (21 clases) |
| `test/` | 40 pruebas unitarias con JUnit 5 |
| `legacy/` | Clase original, para comparar |
| `pom.xml` | Proyecto Maven |

## Resumen del diagnóstico

La clase original concentraba en 45 líneas violaciones a cuatro de los cinco principios:

| Principio | Ubicación | Problema |
|---|---|---|
| SRP | `agregarPecera()` | Gestiona inventario y escribe en la base de datos |
| DIP | `agregarPecera()` | SQL de MySQL embebido en la lógica de negocio |
| OCP | `calcularTotalConImpuesto()` | Cadena `if/else if` por tipo de cliente |
| SRP | `procesarPagoYEnviarNotificacion()` | Cobra el pago y envía el correo en el mismo método |
| OCP | `procesarPagoYEnviarNotificacion()` | Cadena `if/else if` por método de pago |
| DIP | `procesarPagoYEnviarNotificacion()` | Acoplamiento directo a pasarelas y a SMTP |
| ISP | Toda la clase | No existía ninguna abstracción |
| LSP | `procesarPagoYEnviarNotificacion()` | El `else` silencioso hacía creer que el pago fue exitoso |

El detalle de cada caso, con el código antes y después, está en el [documento del taller](Taller_SOLID_TiendaPeceras.md).

## Ejecución

### Demostración

```bash
javac -encoding UTF-8 -d build/classes src/*.java
```

```bash
java -cp build/classes Main
```

### Pruebas unitarias

Con Maven:

```bash
mvn test
```

Sin Maven, usando el jar incluido en `lib/`:

```bash
javac -encoding UTF-8 -cp "build/classes;lib/junit-platform-console-standalone-1.11.4.jar" -d build/test-classes test/*.java
```

```bash
java -jar lib/junit-platform-console-standalone-1.11.4.jar execute --class-path "build/classes;build/test-classes" --scan-class-path build/test-classes
```

Resultado: **40 pruebas, 40 exitosas**, en 117 ms, sin base de datos y sin conexión de red.

## Requisitos

- JDK 17 o superior (probado con JDK 21)
- Maven, opcional (solo para `mvn test`)
