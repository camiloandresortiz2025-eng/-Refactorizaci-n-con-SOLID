/**
 * Descuento VIP del 15%. Sustituye la rama:
 *
 *     if (tipoCliente.equals("VIP")) { ... * 0.85; }
 *
 * El 0.85 era un numero magico; aqui la regla queda nombrada y documentada.
 */
public class DescuentoVip implements PoliticaDescuento {

    private static final double PORCENTAJE_DESCUENTO = 0.15;

    @Override
    public TipoCliente tipoCliente() {
        return TipoCliente.VIP;
    }

    @Override
    public double aplicar(double monto) {
        return monto * (1 - PORCENTAJE_DESCUENTO);
    }
}
