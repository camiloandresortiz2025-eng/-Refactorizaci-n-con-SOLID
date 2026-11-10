import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del servicio de dominio (reemplazo de TiendaPecerasLegacy).
 *
 * Todas corren en milisegundos, sin MySQL, sin pasarelas de pago reales y sin
 * enviar un solo correo. Con el codigo legacy ninguna de estas pruebas era
 * posible: esa imposibilidad era, precisamente, el sintoma del acoplamiento.
 */
@DisplayName("ServicioTiendaPeceras")
class ServicioTiendaPecerasTest {

    private RepositorioPecerasEnMemoria repositorio;
    private NotificadorEspia notificador;
    private CalculadoraPrecio calculadora;

    @BeforeEach
    void prepararEscenario() {
        repositorio = new RepositorioPecerasEnMemoria();
        notificador = new NotificadorEspia();
        calculadora = new CalculadoraPrecio(
                new ImpuestoIvaColombia(),
                Arrays.asList(new SinDescuento(), new DescuentoVip(), new DescuentoMayorista()));
    }

    private ServicioTiendaPeceras servicioCon(List<ProcesadorPago> pasarelas) {
        return new ServicioTiendaPeceras(repositorio, calculadora, notificador, pasarelas);
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Inventario (SRP + DIP)")
    class Inventario {

        @Test
        @DisplayName("agrega la pecera al repositorio inyectado, sin tocar la base de datos real")
        void agregaPeceraAlRepositorio() {
            ServicioTiendaPeceras servicio = servicioCon(Collections.emptyList());

            Pecera pecera = servicio.agregarPecera("Pecera Nano 20L", 150000);

            assertEquals(1, repositorio.listarTodas().size());
            assertEquals("Pecera Nano 20L", pecera.getTipo());
            assertEquals(150000, repositorio.listarTodas().get(0).getPrecio(), 0.001);
        }

        @Test
        @DisplayName("conserva el orden y la cantidad del inventario")
        void consultaElInventarioCompleto() {
            ServicioTiendaPeceras servicio = servicioCon(Collections.emptyList());

            servicio.agregarPecera("Pecera Nano 20L", 150000);
            servicio.agregarPecera("Pecera Plantada 120L", 890000);

            List<Pecera> inventario = servicio.consultarInventario();
            assertEquals(2, inventario.size());
            assertEquals("Pecera Plantada 120L", inventario.get(1).getTipo());
        }

        @Test
        @DisplayName("propaga la validacion del dominio ante datos invalidos")
        void rechazaDatosInvalidos() {
            ServicioTiendaPeceras servicio = servicioCon(Collections.emptyList());

            assertThrows(IllegalArgumentException.class, () -> servicio.agregarPecera("", 1000));
            assertEquals(0, repositorio.listarTodas().size(), "no debe guardar nada si fallo la validacion");
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Procesamiento de la venta (SRP + OCP + DIP)")
    class Venta {

        @Test
        @DisplayName("cobra y notifica cuando el pago es exitoso")
        void cobraYNotifica() {
            ServicioTiendaPeceras servicio =
                    servicioCon(Collections.singletonList(new PagoSimulado(MetodoPago.TARJETA, true)));

            ResultadoPago resultado =
                    servicio.procesarVenta(MetodoPago.TARJETA, 200000, "cliente@correo.com");

            assertTrue(resultado.esExitoso());
            assertEquals(1, notificador.cantidadDeNotificaciones());
            assertEquals("cliente@correo.com", notificador.ultimoDestino());
        }

        @Test
        @DisplayName("NO notifica cuando el pago falla")
        void noNotificaSiElPagoFalla() {
            ServicioTiendaPeceras servicio =
                    servicioCon(Collections.singletonList(new PagoSimulado(MetodoPago.NEQUI, false)));

            ResultadoPago resultado =
                    servicio.procesarVenta(MetodoPago.NEQUI, 200000, "cliente@correo.com");

            assertFalse(resultado.esExitoso());
            assertEquals(0, notificador.cantidadDeNotificaciones());
            assertNull(notificador.ultimoDestino());
        }

        @Test
        @DisplayName("un metodo sin pasarela registrada falla de forma explicita")
        void metodoNoSoportadoLanzaExcepcion() {
            ServicioTiendaPeceras servicio =
                    servicioCon(Collections.singletonList(new PagoSimulado(MetodoPago.TARJETA, true)));

            // En el legacy esto imprimia "Metodo de pago no soportado" y
            // devolvia void, de modo que el llamador creia que habia cobrado.
            assertThrows(MetodoPagoNoSoportadoException.class,
                    () -> servicio.procesarVenta(MetodoPago.PSE, 100000, "cliente@correo.com"));
            assertEquals(0, notificador.cantidadDeNotificaciones());
        }

        @Test
        @DisplayName("delega el cobro a la pasarela que corresponde al metodo elegido")
        void enrutaALaPasarelaCorrecta() {
            PagoSimulado tarjeta = new PagoSimulado(MetodoPago.TARJETA, true);
            PagoSimulado nequi = new PagoSimulado(MetodoPago.NEQUI, true);
            ServicioTiendaPeceras servicio = servicioCon(Arrays.asList(tarjeta, nequi));

            servicio.procesarVenta(MetodoPago.NEQUI, 100000, "cliente@correo.com");

            assertEquals(1, nequi.vecesInvocado());
            assertEquals(0, tarjeta.vecesInvocado(), "la pasarela de tarjeta no debio invocarse");
        }

        @Test
        @DisplayName("OCP: acepta una pasarela nueva sin modificar el servicio")
        void aceptaUnaPasarelaNueva() {
            // PSE no existia en el codigo original y ninguna clase se modifico.
            ServicioTiendaPeceras servicio =
                    servicioCon(Collections.singletonList(new PagoSimulado(MetodoPago.PSE, true)));

            ResultadoPago resultado =
                    servicio.procesarVenta(MetodoPago.PSE, 300000, "cliente@correo.com");

            assertTrue(resultado.esExitoso());
            assertEquals(1, notificador.cantidadDeNotificaciones());
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Flujo completo de venta")
    class FlujoCompleto {

        @Test
        @DisplayName("cotiza con el descuento del cliente y cobra el total correcto")
        void vendeAplicandoElDescuentoVip() {
            PagoSimulado tarjeta = new PagoSimulado(MetodoPago.TARJETA, true);
            ServicioTiendaPeceras servicio = servicioCon(Collections.singletonList(tarjeta));

            Pecera pecera = servicio.agregarPecera("Pecera Plantada 120L", 100000);
            ResultadoPago resultado = servicio.venderPecera(
                    pecera, TipoCliente.VIP, MetodoPago.TARJETA, "vip@correo.com");

            // 100000 + IVA 19% = 119000; menos 15% VIP = 101150
            assertTrue(resultado.esExitoso());
            assertEquals(101150, resultado.getMonto(), 0.001);
        }

        @Test
        @DisplayName("el mayorista paga el total con 25% de descuento")
        void vendeAplicandoElDescuentoMayorista() {
            ServicioTiendaPeceras servicio =
                    servicioCon(Collections.singletonList(new PagoSimulado(MetodoPago.NEQUI, true)));

            Pecera pecera = servicio.agregarPecera("Pecera Marina 200L", 100000);
            ResultadoPago resultado = servicio.venderPecera(
                    pecera, TipoCliente.MAYORISTA, MetodoPago.NEQUI, "mayorista@correo.com");

            assertEquals(89250, resultado.getMonto(), 0.001);
        }
    }

    // ------------------------------------------------------------------
    @Nested
    @DisplayName("Construccion del servicio (DIP)")
    class Construccion {

        @Test
        @DisplayName("exige que todas sus dependencias sean inyectadas")
        void exigeSusDependencias() {
            assertThrows(IllegalArgumentException.class, () -> new ServicioTiendaPeceras(
                    null, calculadora, notificador, Collections.emptyList()));
            assertThrows(IllegalArgumentException.class, () -> new ServicioTiendaPeceras(
                    repositorio, null, notificador, Collections.emptyList()));
            assertThrows(IllegalArgumentException.class, () -> new ServicioTiendaPeceras(
                    repositorio, calculadora, null, Collections.emptyList()));
        }

        @Test
        @DisplayName("funciona igual con el repositorio de MySQL que con el de memoria (LSP)")
        void funcionaConCualquierRepositorio() {
            ServicioTiendaPeceras conMySql = new ServicioTiendaPeceras(
                    new RepositorioPecerasMySql(), calculadora, notificador, Collections.emptyList());

            Pecera pecera = conMySql.agregarPecera("Pecera Nano 20L", 150000);

            assertEquals(1, conMySql.consultarInventario().size());
            assertEquals("Pecera Nano 20L", pecera.getTipo());
        }
    }
}
