import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas del calculo de precios (refactorizacion de calcularTotalConImpuesto).
 *
 * Verifican que los resultados numericos sean IDENTICOS a los del codigo
 * legacy: refactorizar significa mejorar el diseno sin cambiar el
 * comportamiento. Si estos valores cambiaran, no seria una refactorizacion.
 */
@DisplayName("CalculadoraPrecio (OCP mediante Strategy)")
class CalculadoraPrecioTest {

    private static final double PRECIO_BASE = 100000;
    private static final double TOLERANCIA = 0.001;

    private CalculadoraPrecio calculadora;

    @BeforeEach
    void prepararEscenario() {
        List<PoliticaDescuento> politicas = Arrays.asList(
                new SinDescuento(),
                new DescuentoVip(),
                new DescuentoMayorista());
        calculadora = new CalculadoraPrecio(new ImpuestoIvaColombia(), politicas);
    }

    @Test
    @DisplayName("cliente estandar: solo IVA del 19%")
    void clienteEstandarPagaSoloIva() {
        assertEquals(119000, calculadora.calcularTotal(PRECIO_BASE, TipoCliente.ESTANDAR), TOLERANCIA);
    }

    @Test
    @DisplayName("cliente VIP: IVA y 15% de descuento (igual que el legacy)")
    void clienteVipRecibeQuincePorCiento() {
        assertEquals(101150, calculadora.calcularTotal(PRECIO_BASE, TipoCliente.VIP), TOLERANCIA);
    }

    @Test
    @DisplayName("cliente mayorista: IVA y 25% de descuento (igual que el legacy)")
    void clienteMayoristaRecibeVeinticincoPorCiento() {
        assertEquals(89250, calculadora.calcularTotal(PRECIO_BASE, TipoCliente.MAYORISTA), TOLERANCIA);
    }

    @Test
    @DisplayName("si un tipo de cliente no tiene politica registrada, no se le aplica descuento")
    void usaElNullObjectCuandoNoHayPolitica() {
        CalculadoraPrecio sinPoliticas =
                new CalculadoraPrecio(new ImpuestoIvaColombia(), Collections.emptyList());

        assertEquals(119000, sinPoliticas.calcularTotal(PRECIO_BASE, TipoCliente.VIP), TOLERANCIA);
    }

    @Test
    @DisplayName("la tarifa de impuesto es parametrizable (ya no es un numero magico)")
    void permiteCambiarLaTarifaDeImpuesto() {
        CalculadoraPrecio conIvaCinco =
                new CalculadoraPrecio(new ImpuestoIvaColombia(0.05), Collections.emptyList());

        assertEquals(105000, conIvaCinco.calcularTotal(PRECIO_BASE, TipoCliente.ESTANDAR), TOLERANCIA);
    }

    @Test
    @DisplayName("rechaza un precio base negativo")
    void rechazaPrecioNegativo() {
        assertThrows(IllegalArgumentException.class,
                () -> calculadora.calcularTotal(-100, TipoCliente.VIP));
    }

    @Test
    @DisplayName("OCP: se agrega una politica nueva sin modificar CalculadoraPrecio")
    void aceptaUnaPoliticaNuevaSinModificarLaClase() {
        // Politica inexistente en el diseno original: descuento del 10% al
        // cliente estandar. No se modifico NINGUNA clase para soportarla.
        PoliticaDescuento descuentoEstudiante = new PoliticaDescuento() {
            @Override
            public TipoCliente tipoCliente() {
                return TipoCliente.ESTANDAR;
            }

            @Override
            public double aplicar(double monto) {
                return monto * 0.90;
            }
        };

        CalculadoraPrecio ampliada = new CalculadoraPrecio(
                new ImpuestoIvaColombia(),
                Arrays.asList(descuentoEstudiante, new DescuentoVip()));

        assertEquals(107100, ampliada.calcularTotal(PRECIO_BASE, TipoCliente.ESTANDAR), TOLERANCIA);
        // Las politicas que ya existian siguen intactas:
        assertEquals(101150, ampliada.calcularTotal(PRECIO_BASE, TipoCliente.VIP), TOLERANCIA);
    }
}
