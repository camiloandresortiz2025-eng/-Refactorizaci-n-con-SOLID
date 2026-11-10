/**
 * Descuento mayorista del 25%. Sustituye la rama:
 *
 *     else if (tipoCliente.equals("MAYORISTA")) { ... * 0.75; }
 */
public class DescuentoMayorista implements PoliticaDescuento {

    private static final double PORCENTAJE_DESCUENTO = 0.25;

    @Override
    public TipoCliente tipoCliente() {
        return TipoCliente.MAYORISTA;
    }

    @Override
    public double aplicar(double monto) {
        return monto * (1 - PORCENTAJE_DESCUENTO);
    }
}
