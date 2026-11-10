# Taller de Refactorización SOLID — TiendaPecerasLegacy

---

## 1. Actividad de Reflexión Inicial (punto 3.1)

**Escenario:** un equipo debe agregar un nuevo método de pago (Nequi) a un sistema de comercio electrónico. Para lograrlo el programador tuvo que modificar 12 clases, rompió el módulo de facturación y tardó 3 días corrigiendo errores colaterales.

### ¿Por qué la modificación de una funcionalidad provocó fallos en partes no relacionadas del sistema?

Porque en realidad esas partes **sí estaban relacionadas**, aunque en el diagrama se vieran separadas. El código estaba tan acoplado que la lógica de pagos vivía mezclada con la de facturación, la de base de datos y la de notificaciones dentro de las mismas clases. Cuando uno toca una clase que hace cinco cosas distintas, está tocando esas cinco cosas al mismo tiempo, aunque solo quiera cambiar una.

Lo que probablemente pasó es que el método de pago estaba resuelto con cadenas de `if / else if` repartidas por todo el sistema: un `if` para decidir cómo cobrar, otro para decidir qué imprimir en la factura, otro para decidir qué correo enviar. Agregar Nequi obligaba a buscar cada uno de esos `if` y agregarle una rama más. Por eso fueron 12 clases: no es que el cambio fuera grande, es que el mismo conocimiento ("qué métodos de pago existen") estaba duplicado en 12 lugares. Y cuando una de esas 12 ediciones se hace mal, o se olvida, el sistema falla en un sitio que el programador nunca estuvo mirando: facturación.

También influye que no había abstracciones de por medio. Si las clases de alto nivel hubieran dependido de una interfaz como `MedioDePago`, agregar Nequi habría sido **crear** una clase nueva, no **modificar** las existentes. El código que ya funcionaba se habría quedado quieto, y lo que no se toca no se rompe. Sin esa interfaz, cada clase conocía los detalles concretos de cada pasarela, y por eso el cambio se propagó como una onda.

Y hay un detalle que explica los 3 días de corrección: seguramente no había pruebas automatizadas. Con clases que instancian su propia conexión a MySQL y envían correos reales, no se puede escribir un test unitario; hay que probar a mano, y los errores colaterales solo aparecen cuando alguien los encuentra por casualidad.

### ¿Cómo se relaciona este problema con la calidad y mantenibilidad del software?

Este escenario es el ejemplo de libro de los tres "olores" que la ingeniería de software considera síntomas de mala calidad de diseño:

- **Rigidez:** un cambio pequeño cuesta mucho esfuerzo (3 días por un método de pago). El sistema se resiste a ser modificado.
- **Fragilidad:** al cambiar algo, se rompe algo que no tiene relación aparente (el módulo de facturación). El equipo pierde la confianza en el código y empieza a tenerle miedo a tocarlo.
- **Inmovilidad:** nada se puede reutilizar aparte, porque todo viene pegado con todo.

En términos de calidad del software, esto afecta directamente los atributos de **mantenibilidad**, **modificabilidad** y **capacidad de prueba** (los que mide la ISO/IEC 25010). Un sistema así no se puede integrar a un flujo de integración continua, porque no hay pruebas automatizadas que garanticen que un cambio no rompió lo demás; cada despliegue se vuelve una apuesta.

El costo es acumulativo: lo que hoy son 3 días, el próximo cambio serán 5, porque cada parche añade más acoplamiento. Esto es lo que se conoce como **deuda técnica**. Los principios SOLID son precisamente las reglas de diseño que evitan llegar a ese punto: separar responsabilidades (SRP) para que un cambio tenga un solo lugar donde ocurrir, y depender de abstracciones (DIP/OCP) para que lo nuevo se agregue sin tocar lo viejo. Aplicarlos no es "escribir bonito": es lo que hace que el software siga siendo barato de cambiar dentro de dos años.

---

## 2. Diagnóstico del código base: `TiendaPecerasLegacy.java`

La clase original tiene 45 líneas y tres métodos, y en ese espacio tan corto concentra violaciones a **cuatro** de los cinco principios SOLID. A continuación el diagnóstico, violación por violación.

### Resumen

| # | Principio violado | Ubicación | Síntoma |
|---|---|---|---|
| 1 | **SRP** | `agregarPecera()` | La clase gestiona inventario **y** escribe en la base de datos |
| 2 | **DIP** | `agregarPecera()` | Dependencia directa a MySQL (SQL embebido en la clase) |
| 3 | **OCP** | `calcularTotalConImpuesto()` | Cadena `if/else if` por tipo de cliente |
| 4 | **SRP** | `procesarPagoYEnviarNotificacion()` | Cobra el pago **y** envía el correo (el nombre lo confiesa con la "Y") |
| 5 | **OCP** | `procesarPagoYEnviarNotificacion()` | Cadena `if/else if` por método de pago |
| 6 | **DIP** | `procesarPagoYEnviarNotificacion()` | Acoplamiento directo a la pasarela de tarjeta, a la API de Nequi y al SMTP |
| 7 | **ISP** | Toda la clase | No existe ninguna abstracción; el cliente depende de la clase concreta completa |
| 8 | **LSP** | `procesarPagoYEnviarNotificacion()` | El `else` silencioso rompe el contrato: el método "procesa" sin procesar nada |
| 9 | Modelado | `List<String> inventarioPeceras` | El dominio se guarda como texto concatenado, no como objeto |

---

### Violación 1 y 2 — SRP y DIP en `agregarPecera()`

**Código original:**

```java
private List<String> inventarioPeceras = new ArrayList<>();

public void agregarPecera(String tipoPecera, double precio) {
    inventarioPeceras.add(tipoPecera + " - $" + precio);

    // Acoplamiento directo a MySQL
    System.out.println("[DB MySQL] INSERT INTO peceras (tipo, precio) VALUES ('" + tipoPecera + "', " + precio + ")");
}
```

**¿Por qué está mal?**

- **SRP (Single Responsibility Principle):** el método tiene dos razones distintas para cambiar. Si mañana cambia la regla de negocio del inventario, hay que editarlo; y si cambia el motor de base de datos, hay que editar exactamente el mismo método. Son dos áreas de responsabilidad (negocio y persistencia) viviendo en el mismo sitio.
- **DIP (Dependency Inversion Principle):** la clase de alto nivel (la tienda, que es el negocio) depende de un detalle de bajo nivel (MySQL y su dialecto SQL). La flecha de dependencia apunta en el sentido equivocado. Migrar a PostgreSQL, a MongoDB o a una API REST significa reescribir el núcleo del negocio.
- **Consecuencia práctica:** esta clase es imposible de probar unitariamente. No se puede verificar que una pecera se agregó al inventario sin tocar la base de datos.
- **Bonus de modelado:** guardar `"Pecera Nano - $150.0"` como `String` destruye la información. Después nadie puede ordenar por precio, filtrar por tipo ni sumar el inventario sin ponerse a partir cadenas de texto.

**Refactorización aplicada:**

Se crea la entidad de dominio `Pecera`, se extrae la persistencia a la interfaz `PeceraRepository`, y la tienda recibe la implementación por constructor (inyección de dependencias).

```java
public interface PeceraRepository {
    void guardar(Pecera pecera);
    List<Pecera> listarTodas();
}
```

```java
public class ServicioTiendaPeceras {
    private final PeceraRepository repositorio;   // depende de la ABSTRACCIÓN

    public ServicioTiendaPeceras(PeceraRepository repositorio, ...) {
        this.repositorio = repositorio;           // DIP: se inyecta desde afuera
    }

    public Pecera agregarPecera(String tipo, double precio) {
        Pecera pecera = new Pecera(tipo, precio); // SRP: solo lógica de negocio
        repositorio.guardar(pecera);              // la persistencia es de otro
        return pecera;
    }
}
```

**¿Qué ganamos?** `ServicioTiendaPeceras` ya no sabe —ni le importa— si detrás hay MySQL, PostgreSQL o una lista en memoria. Para las pruebas se le pasa `RepositorioPecerasEnMemoria` y el test corre en milisegundos, sin base de datos. Cambiar de motor es escribir una clase nueva, sin tocar el negocio.

---

### Violación 3 — OCP en `calcularTotalConImpuesto()`

**Código original:**

```java
public double calcularTotalConImpuesto(double precioBase, String tipoCliente) {
    double impuesto = 0.19;

    if (tipoCliente.equals("VIP")) {
        return (precioBase * (1 + impuesto)) * 0.85;
    } else if (tipoCliente.equals("MAYORISTA")) {
        return (precioBase * (1 + impuesto)) * 0.75;
    }

    return precioBase * (1 + impuesto);
}
```

**¿Por qué está mal?**

- **OCP (Open/Closed Principle):** la clase debería estar **abierta a extensión pero cerrada a modificación**, y aquí es exactamente al revés. Agregar un cliente "ESTUDIANTE" con 10% de descuento obliga a abrir este método y meter otra rama `else if`. Cada rama nueva es un riesgo de romper las anteriores y obliga a volver a probar todo el método.
- El `tipoCliente` es un `String` libre: `"vip"` en minúscula, `"Vip"` o un espacio de más caen silenciosamente en el caso sin descuento, y nadie se da cuenta hasta que un cliente VIP reclama. Además `tipoCliente` puede llegar `null` y revienta con `NullPointerException` en el `.equals()`.
- Los números `0.19`, `0.85` y `0.75` están incrustados en el código (*magic numbers*). La regla de negocio no está documentada en ninguna parte y se repite la operación `(precioBase * (1 + impuesto))` tres veces.

**Refactorización aplicada:**

Se aplica el patrón **Strategy**: cada política de descuento es su propia clase que implementa una interfaz común, y el tipo de cliente pasa a ser un `enum` en vez de un `String` suelto. El impuesto también se extrae a su propia abstracción, porque el IVA es una regla que cambia por ley y no debería estar quemada en el cálculo.

```java
public interface PoliticaDescuento {
    TipoCliente tipoCliente();
    double aplicar(double monto);
}

public class DescuentoVip implements PoliticaDescuento {
    private static final double PORCENTAJE_DESCUENTO = 0.15;

    @Override public TipoCliente tipoCliente() { return TipoCliente.VIP; }
    @Override public double aplicar(double monto) { return monto * (1 - PORCENTAJE_DESCUENTO); }
}
```

```java
public class CalculadoraPrecio {
    private final PoliticaImpuesto impuesto;
    private final Map<TipoCliente, PoliticaDescuento> descuentos;

    public double calcularTotal(double precioBase, TipoCliente tipoCliente) {
        double conImpuesto = impuesto.aplicar(precioBase);
        PoliticaDescuento politica = descuentos.getOrDefault(tipoCliente, new SinDescuento());
        return politica.aplicar(conImpuesto);   // polimorfismo, cero if/else
    }
}
```

**¿Qué ganamos?** Para agregar el cliente "ESTUDIANTE" se crea `DescuentoEstudiante` y se registra; `CalculadoraPrecio` **no se modifica ni se vuelve a compilar**. Eso es OCP funcionando. Y como el tipo de cliente es un `enum`, un valor inválido ya no compila: el error se detecta en tiempo de compilación y no en producción.

---

### Violación 4, 5 y 6 — SRP, OCP y DIP en `procesarPagoYEnviarNotificacion()`

**Código original:**

```java
public void procesarPagoYEnviarNotificacion(String metodoPago, double monto, String emailCliente) {
    if (metodoPago.equals("TARJETA")) {
        System.out.println("Conectando con pasarela de pago con tarjeta... Procesando $" + monto);
    } else if (metodoPago.equals("NEQUI")) {
        System.out.println("Conectando con API de Nequi... Procesando $" + monto);
    } else {
        System.out.println("Método de pago no soportado.");
        return;
    }

    System.out.println("Enviando correo SMTP a " + emailCliente + ": Su pago de $" + monto + " fue exitoso.");
}
```

Este método es el corazón del problema y reproduce **exactamente** el escenario de la actividad 3.1: es el código donde agregar Nequi obliga a abrir la clase.

**¿Por qué está mal?**

- **SRP:** el nombre del método lo delata — `procesarPago` **Y** `enviarNotificacion`. Cuando el nombre de un método necesita una "y", está haciendo dos cosas. Tiene dos razones para cambiar: que cambie la pasarela de pagos, o que cambie el canal de notificación (si mañana notifican por WhatsApp, hay que editar un método que se llama "procesar pago", lo cual no tiene ningún sentido).
- **OCP:** otra cadena `if/else if`, esta vez por método de pago. Agregar Daviplata o PSE es modificar esta clase. Es el mismo defecto que causó los 3 días de corrección del escenario inicial.
- **DIP:** la clase conoce los detalles concretos de la pasarela de tarjeta, de la API de Nequi y del servidor SMTP. El negocio depende de tres sistemas externos al mismo tiempo.
- **Testabilidad:** no se puede probar la lógica de venta sin cobrar un pago real y mandar un correo real.
- **LSP (de forma indirecta):** el `else` imprime un mensaje y hace `return`. El método anuncia que procesa un pago y en ese camino no procesa nada, pero tampoco avisa al llamador: devuelve `void` y el flujo continúa como si todo hubiera salido bien. Quien lo llame creerá que cobró. Un pago fallido debe ser un resultado explícito o una excepción, nunca un `System.out.println`.

**Refactorización aplicada:**

Se separan las dos responsabilidades en dos jerarquías independientes y se devuelve un resultado explícito.

```java
public interface ProcesadorPago {        // una interfaz por pasarela (OCP + DIP)
    MetodoPago metodoSoportado();
    ResultadoPago procesar(double monto);
}

public interface Notificador {           // interfaz pequeña y enfocada (ISP)
    void notificar(String destino, String mensaje);
}
```

```java
public class ServicioTiendaPeceras {
    private final Map<MetodoPago, ProcesadorPago> procesadores;
    private final Notificador notificador;

    public ResultadoPago procesarVenta(MetodoPago metodo, double monto, String emailCliente) {
        ProcesadorPago procesador = procesadores.get(metodo);
        if (procesador == null) {
            throw new MetodoPagoNoSoportadoException(metodo);   // falla explícita, no silenciosa
        }

        ResultadoPago resultado = procesador.procesar(monto);    // polimorfismo

        if (resultado.esExitoso()) {
            notificador.notificar(emailCliente, resultado.mensaje());  // responsabilidad separada
        }
        return resultado;                                        // el llamador SÍ se entera
    }
}
```

**¿Qué ganamos?** Agregar Daviplata es crear `PagoDaviplata implements ProcesadorPago` y registrarlo en el arranque de la aplicación. `ServicioTiendaPeceras` no se toca. Pasar de correo a SMS es crear `NotificadorSms implements Notificador`. Y como el resultado se devuelve, el que llama puede decidir qué hacer cuando el pago falla, en vez de enterarse por un mensaje en la consola.

---

### Violación 7 — ISP (Interface Segregation Principle)

En el código original **no existe ni una sola interfaz**. Cualquier clase que quisiera usar la tienda tendría que depender de `TiendaPecerasLegacy` completa: inventario, SQL, pasarelas y correos, todo junto. Un módulo que solo necesita calcular un precio queda arrastrando por transitividad la dependencia a MySQL y al SMTP.

El ISP dice que los clientes no deben depender de métodos que no usan. La refactorización crea **interfaces pequeñas y con un solo propósito**:

| Interfaz | Métodos | Para quién |
|---|---|---|
| `PeceraRepository` | `guardar`, `listarTodas` | quien persiste |
| `PoliticaImpuesto` | `aplicar` | quien calcula impuestos |
| `PoliticaDescuento` | `tipoCliente`, `aplicar` | quien aplica descuentos |
| `ProcesadorPago` | `metodoSoportado`, `procesar` | quien cobra |
| `Notificador` | `notificar` | quien avisa al cliente |

Lo que se evitó a propósito fue crear una interfaz "gorda" tipo `IServicioTienda` con los nueve métodos adentro: eso habría cambiado el nombre del problema sin resolverlo.

---

### Violación 8 — LSP (Liskov Substitution Principle)

El código legacy, al no tener herencia ni interfaces, no puede violar el LSP de forma literal: no hay subtipos que sustituir. Pero sí tiene el defecto que el LSP busca prevenir, y es importante no reintroducirlo al refactorizar.

El principio exige que **cualquier implementación de `ProcesadorPago` se pueda usar donde se espera un `ProcesadorPago`, sin que el programa cambie su corrección**. Para que eso se cumpla, el contrato de la interfaz se documentó de forma explícita:

- Toda implementación **siempre** devuelve un `ResultadoPago` no nulo (nunca `null`, nunca un `return` silencioso como el `else` del original).
- Ninguna implementación puede endurecer las precondiciones: si la interfaz acepta cualquier monto positivo, una implementación concreta no puede exigir "solo montos mayores a $50.000" y fallar con el resto, porque el código que la usa no tiene forma de saberlo.
- El fallo de negocio (fondos insuficientes, pasarela caída) se reporta por el mismo canal en todas: un `ResultadoPago` con `esExitoso() == false`.

Gracias a eso, `ServicioTiendaPeceras` trata a `PagoTarjeta`, `PagoNequi` y a cualquier pasarela futura exactamente igual, y el `if (resultado.esExitoso())` funciona para todas sin casos especiales. Si alguna implementación necesitara un trato aparte, eso sería la señal de que el LSP se rompió.

---

## 3. Estructura de la solución refactorizada

```
Taller_SOLID_Peceras/
├── Taller_SOLID_TiendaPeceras.md   Este documento
├── pom.xml                         Proyecto Maven (para ejecutar las pruebas)
├── legacy/                         Código original, para comparar
├── lib/                            JUnit 5 standalone
├── test/                           Pruebas unitarias (ver sección 4)
└── src/                            Código refactorizado
```

Detalle del código fuente:

```
src/
├── Pecera.java                            Entidad de dominio
├── TipoCliente.java                       enum (reemplaza el String suelto)
├── MetodoPago.java                        enum (reemplaza el String suelto)
├── ResultadoPago.java                     Resultado explícito del cobro
├── MetodoPagoNoSoportadoException.java    Falla explícita en vez de println
│
├── PeceraRepository.java                  [interfaz]  DIP + ISP
├── RepositorioPecerasMySql.java           implementación de persistencia
├── RepositorioPecerasEnMemoria.java       implementación para pruebas
│
├── PoliticaImpuesto.java                  [interfaz]
├── ImpuestoIvaColombia.java               IVA 19% (parametrizable)
│
├── PoliticaDescuento.java                 [interfaz]  Strategy → OCP
├── SinDescuento.java                      cliente estándar
├── DescuentoVip.java                      15%
├── DescuentoMayorista.java                25%
├── CalculadoraPrecio.java                 orquesta impuesto + descuento
│
├── ProcesadorPago.java                    [interfaz]  Strategy → OCP + DIP
├── PagoTarjeta.java                       pasarela de tarjeta
├── PagoNequi.java                         API Nequi
│
├── Notificador.java                       [interfaz]
├── NotificadorEmail.java                  canal SMTP
│
├── ServicioTiendaPeceras.java             servicio de dominio desacoplado (SRP)
└── Main.java                              composición de dependencias + demo
```

### Dónde quedó cada principio

| Principio | Cómo se cumple en la solución |
|---|---|
| **S**RP | Cada clase tiene una sola razón para cambiar. `ServicioTiendaPeceras` coordina, no calcula impuestos, no guarda en BD y no envía correos. |
| **O**CP | Nuevos descuentos, métodos de pago o canales de notificación se agregan creando clases y registrándolas en `Main`. Ninguna clase existente se modifica. |
| **L**SP | Las implementaciones de `ProcesadorPago`, `PoliticaDescuento` y `Notificador` respetan el mismo contrato y son intercambiables sin casos especiales. |
| **I**SP | Cinco interfaces pequeñas en vez de una clase monolítica o una interfaz "gorda". Cada cliente depende solo de lo que usa. |
| **D**IP | `ServicioTiendaPeceras` y `CalculadoraPrecio` dependen únicamente de interfaces; las implementaciones concretas se inyectan por constructor desde `Main`. |

### Prueba de que el diseño es extensible

La clase `Main` incluye una demostración: registra una pasarela adicional (`PagoPse`, definida en el propio `Main` como clase anidada) **sin modificar una sola línea** de `ServicioTiendaPeceras` ni de ninguna otra clase ya escrita. Ese es el criterio objetivo de que el OCP se cumple.

### Cómo compilar y ejecutar

```bash
cd Taller_SOLID_Peceras/src
javac *.java -d ../build
java -cp ../build Main
```

### Salida verificada de la ejecución

El proyecto fue compilado con JDK 21 y ejecutado; esta es la salida real:

```
=== 1. Registro de inventario ===
[DB MySQL] INSERT INTO peceras (tipo, precio) VALUES ('Pecera Nano 20L', 150000,00)
[DB MySQL] INSERT INTO peceras (tipo, precio) VALUES ('Pecera Plantada 120L', 890000,00)
Inventario actual: [Pecera{tipo='Pecera Nano 20L', precio=150000,00}, Pecera{tipo='Pecera Plantada 120L', precio=890000,00}]

=== 2. Cotizaciones por tipo de cliente ===
ESTANDAR   -> total: $178500,00
VIP        -> total: $151725,00
MAYORISTA  -> total: $133875,00

=== 3. Venta con tarjeta a cliente VIP ===
Conectando con pasarela de pago con tarjeta... Procesando $900235,00
Enviando correo SMTP a cliente.vip@correo.com: Su pago de $900235,00 con tarjeta fue exitoso.
Resultado: [OK] Su pago de $900235,00 con tarjeta fue exitoso.

=== 4. Venta por Nequi a mayorista ===
Conectando con API de Nequi... Procesando $794325,00
Enviando correo SMTP a mayorista@acuarios.com: Su pago de $794325,00 por Nequi fue exitoso.
Resultado: [OK] Su pago de $794325,00 por Nequi fue exitoso.

=== 5. Metodo de pago sin pasarela registrada ===
Error controlado: No hay una pasarela registrada para el metodo de pago: PSE

=== 6. Extension: se agrega PSE sin tocar el servicio ===
Redirigiendo al portal PSE del banco... Procesando $100000,00
Enviando correo SMTP a cliente@correo.com: Su pago de $100000,00 por PSE fue exitoso.
Resultado: [OK] Su pago de $100000,00 por PSE fue exitoso.

=== 7. El mismo servicio con repositorio en memoria (pruebas) ===
Registros en memoria (sin tocar MySQL): 1
```

Los puntos 5, 6 y 7 son la evidencia objetiva del taller: un fallo que ya no pasa inadvertido, una pasarela nueva agregada sin modificar código existente, y el mismo servicio funcionando sin base de datos.

---

## 4. Pruebas unitarias (JUnit 5)

La guía menciona que el objetivo final es dejar el código "listo para pruebas automatizadas e integración continua". Las pruebas son, además, la **evidencia objetiva** de que la refactorización funcionó: con el código legacy **ninguna de ellas podía escribirse**, porque probar la tienda implicaba conectarse a MySQL, llamar a una pasarela de pagos real y enviar un correo real.

### Suite implementada — carpeta `test/`

| Archivo | Qué verifica | Pruebas |
|---|---|---|
| `PeceraTest.java` | La entidad de dominio conserva los datos (ya no texto concatenado) y valida sus invariantes | 4 |
| `CalculadoraPrecioTest.java` | Los totales son **idénticos** a los del legacy, la tarifa es parametrizable y se puede agregar una política nueva sin modificar la clase | 7 |
| `ProcesadorPagoContratoTest.java` | Prueba de **contrato LSP**: las mismas aserciones corren contra *todas* las pasarelas | 15 |
| `ServicioTiendaPecerasTest.java` | Inventario, enrutamiento de pagos, notificación condicionada al éxito, fallo explícito e inyección de dependencias | 12 |
| `NotificadorEmailTest.java` | El canal de notificación es probable por separado | 2 |
| `NotificadorEspia.java` | *Test double* que registra los correos en vez de enviarlos | — |
| `PagoSimulado.java` | *Test double* que simula cobros exitosos y fallidos | — |

**Total: 40 pruebas.**

### Las tres pruebas que demuestran cada principio

```java
// SRP + DIP: la venta se prueba sin base de datos y sin enviar correos
@Test
void agregaPeceraAlRepositorio() {
    // se inyectan dobles en vez de MySQL y SMTP
    ServicioTiendaPeceras servicio = servicioCon(Collections.emptyList());
    servicio.agregarPecera("Pecera Nano 20L", 150000);
    assertEquals(1, repositorio.listarTodas().size());
}

// OCP: se agrega una política de descuento que no existía,
// sin modificar ni recompilar CalculadoraPrecio
@Test
void aceptaUnaPoliticaNuevaSinModificarLaClase() { ... }

// LSP: la MISMA prueba corre contra todas las implementaciones
@ParameterizedTest
@MethodSource("pasarelas")
void elFalloEsExplicito(ProcesadorPago pasarela) {
    assertFalse(pasarela.procesar(0).esExitoso());
}
```

Y la prueba que ataca directamente el defecto del `else` silencioso del legacy:

```java
@Test
@DisplayName("NO notifica cuando el pago falla")
void noNotificaSiElPagoFalla() {
    ResultadoPago resultado = servicio.procesarVenta(MetodoPago.NEQUI, 200000, "cliente@correo.com");

    assertFalse(resultado.esExitoso());
    assertEquals(0, notificador.cantidadDeNotificaciones());  // no se le miente al cliente
}
```

En el código original esto era imposible de garantizar: el método imprimía "Método de pago no soportado", hacía `return` y el llamador seguía creyendo que había cobrado.

### Cómo ejecutar las pruebas

**Opción A — con Maven** (el `pom.xml` ya está configurado; Maven descarga JUnit solo):

```bash
mvn test
```

**Opción B — sin Maven**, con el jar incluido en `lib/`:

```bash
javac -encoding UTF-8 -d build/classes src/*.java
```

```bash
javac -encoding UTF-8 -cp "build/classes;lib/junit-platform-console-standalone-1.11.4.jar" -d build/test-classes test/*.java
```

```bash
java -jar lib/junit-platform-console-standalone-1.11.4.jar execute --class-path "build/classes;build/test-classes" --scan-class-path build/test-classes
```

**Opción C — NetBeans / IntelliJ / Eclipse:** abrir la carpeta como proyecto Maven y ejecutar *Run Tests*.

### Resultado real de la ejecución

```
├─ ServicioTiendaPeceras ✔
│  ├─ Construccion del servicio (DIP) ✔
│  ├─ Flujo completo de venta ✔
│  ├─ Procesamiento de la venta (SRP + OCP + DIP) ✔
│  └─ Inventario (SRP + DIP) ✔
├─ NotificadorEmail ✔
├─ Entidad Pecera ✔
├─ Contrato de ProcesadorPago (LSP) ✔
│  ├─ reporta el fallo sin lanzar excepciones ante un monto invalido ✔
│  │  ├─ PagoTarjeta ✔
│  │  ├─ PagoNequi ✔
│  │  └─ PagoSimulado(PSE) ✔
│  └─ ...
└─ CalculadoraPrecio (OCP mediante Strategy) ✔

Test run finished after 117 ms
[ 40 tests found           ]
[ 40 tests successful      ]
[  0 tests failed          ]
```

La suite completa corre en **117 milisegundos**, sin base de datos, sin red y sin enviar correos. Ese dato es el argumento más fuerte del taller: un sistema así se puede integrar a un flujo de integración continua, y el legacy no.

> **Nota de honestidad técnica:** en la primera ejecución 2 de las 40 pruebas fallaron. El problema no estaba en el diseño sino en el doble `PagoSimulado`, que aceptaba montos inválidos y por tanto **no respetaba el contrato** de `ProcesadorPago`. Es un ejemplo real de para qué sirve una prueba de contrato LSP: detectó una implementación que no era sustituible por las demás. Se corrigió el doble y la suite quedó en verde.

---

## 5. Conclusión del taller

La clase original no estaba "mal escrita" en el sentido de tener errores de sintaxis: compilaba y funcionaba. El problema es que estaba construida de una forma que hace **caro cualquier cambio futuro**, y en software el cambio es la única certeza. Las 45 líneas del legacy pasaron a varias clases pequeñas, y a primera vista eso parece más complicado; pero el resultado es que cada pieza se puede entender, probar y reemplazar por separado, y que lo nuevo se agrega sin arriesgar lo que ya funciona.

Esa es la diferencia que los principios SOLID aportan al aseguramiento de la calidad: no se trata de estética, sino de mantener bajo el costo del cambio y de hacer el código **probable**, que es el requisito para poder automatizar pruebas e integrarlas a un flujo de integración continua.
