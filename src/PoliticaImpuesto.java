/**
 * Abstraccion del impuesto.
 *
 * En el legacy el 0.19 estaba quemado dentro del metodo de calculo. El IVA es
 * una regla que cambia por ley y que puede diferir por producto o por pais, asi
 * que no pertenece al cuerpo de un calculo de negocio.
 */
public interface PoliticaImpuesto {

    /** Devuelve el monto con el impuesto ya aplicado. */
    double aplicar(double montoBase);
}
