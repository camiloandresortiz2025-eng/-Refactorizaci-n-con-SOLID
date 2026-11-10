import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Refactorizacion de calcularTotalConImpuesto().
 *
 * Que se corrigio:
 *  - OCP: desaparecio la cadena if / else if. Las politicas se registran en un
 *    mapa y se resuelven por polimorfismo, asi que agregar un tipo de cliente
 *    nuevo no obliga a modificar (ni recompilar) esta clase.
 *  - SRP: esta clase solo calcula precios. No conoce impuestos ni descuentos en
 *    detalle, solo los coordina.
 *  - DIP: depende de las abstracciones PoliticaImpuesto y PoliticaDescuento.
 *  - Numeros magicos: 0.19 / 0.85 / 0.75 ya no estan aqui.
 */
public class CalculadoraPrecio {

    private final PoliticaImpuesto impuesto;
    private final Map<TipoCliente, PoliticaDescuento> descuentos = new EnumMap<>(TipoCliente.class);
    private final PoliticaDescuento descuentoPorDefecto = new SinDescuento();

    public CalculadoraPrecio(PoliticaImpuesto impuesto, List<PoliticaDescuento> politicas) {
        if (impuesto == null) {
            throw new IllegalArgumentException("Se requiere una politica de impuesto.");
        }
        this.impuesto = impuesto;
        if (politicas != null) {
            for (PoliticaDescuento politica : politicas) {
                this.descuentos.put(politica.tipoCliente(), politica);
            }
        }
    }

    public double calcularTotal(double precioBase, TipoCliente tipoCliente) {
        if (precioBase < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo.");
        }
        double montoConImpuesto = impuesto.aplicar(precioBase);
        PoliticaDescuento politica = descuentos.getOrDefault(tipoCliente, descuentoPorDefecto);
        return politica.aplicar(montoConImpuesto);
    }
}
