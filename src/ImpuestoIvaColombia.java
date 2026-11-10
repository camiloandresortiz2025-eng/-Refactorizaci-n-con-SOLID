/**
 * IVA colombiano. La tarifa es un parametro, no un numero magico en medio del
 * calculo: si cambia la ley se cambia el valor en un solo lugar (Main) y el
 * resto del sistema sigue igual.
 */
public class ImpuestoIvaColombia implements PoliticaImpuesto {

    public static final double TARIFA_GENERAL = 0.19;

    private final double tarifa;

    public ImpuestoIvaColombia() {
        this(TARIFA_GENERAL);
    }

    public ImpuestoIvaColombia(double tarifa) {
        if (tarifa < 0) {
            throw new IllegalArgumentException("La tarifa de impuesto no puede ser negativa.");
        }
        this.tarifa = tarifa;
    }

    @Override
    public double aplicar(double montoBase) {
        return montoBase * (1 + tarifa);
    }

    public double getTarifa() {
        return tarifa;
    }
}
