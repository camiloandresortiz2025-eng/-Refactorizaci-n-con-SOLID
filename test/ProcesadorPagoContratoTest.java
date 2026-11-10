import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Prueba de contrato (LSP).
 *
 * En lugar de probar cada pasarela por separado, el mismo conjunto de
 * aserciones se ejecuta contra TODAS las implementaciones de ProcesadorPago.
 * Si manana se agrega una pasarela nueva, basta registrarla en el metodo
 * pasarelas() y queda cubierta.
 *
 * Esto es exactamente lo que el principio de sustitucion de Liskov exige: que
 * cualquier subtipo se pueda usar donde se espera el tipo base sin alterar la
 * correccion del programa. Y es la prueba que el codigo legacy no podia tener,
 * porque su "else" imprimia en consola y devolvia void.
 */
@DisplayName("Contrato de ProcesadorPago (LSP)")
class ProcesadorPagoContratoTest {

    static Stream<ProcesadorPago> pasarelas() {
        return Stream.of(
                new PagoTarjeta(),
                new PagoNequi(),
                new PagoSimulado(MetodoPago.PSE, true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pasarelas")
    @DisplayName("nunca devuelve null")
    void nuncaDevuelveNull(ProcesadorPago pasarela) {
        assertNotNull(pasarela.procesar(50000));
        assertNotNull(pasarela.procesar(0), "incluso ante un monto invalido debe devolver un resultado");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pasarelas")
    @DisplayName("declara siempre el mismo metodo soportado")
    void declaraSuMetodoDePago(ProcesadorPago pasarela) {
        assertNotNull(pasarela.metodoSoportado());
        assertEquals(pasarela.metodoSoportado(), pasarela.metodoSoportado());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pasarelas")
    @DisplayName("cobra correctamente un monto valido y conserva el monto")
    void cobraUnMontoValido(ProcesadorPago pasarela) {
        ResultadoPago resultado = pasarela.procesar(250000);

        assertTrue(resultado.esExitoso());
        assertEquals(250000, resultado.getMonto(), 0.001);
        assertNotNull(resultado.getMensaje());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pasarelas")
    @DisplayName("reporta el fallo sin lanzar excepciones ante un monto invalido")
    void rechazaMontoInvalidoSinExplotar(ProcesadorPago pasarela) {
        ResultadoPago resultado = pasarela.procesar(-1);

        assertFalse(resultado.esExitoso());
        assertNotNull(resultado.getMensaje());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("pasarelas")
    @DisplayName("un pago fallido nunca se reporta como exitoso")
    void elFalloEsExplicito(ProcesadorPago pasarela) {
        assertFalse(pasarela.procesar(0).esExitoso());
    }
}
