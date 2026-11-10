/**
 * Cliente estandar: no hay descuento.
 *
 * Es el patron Null Object. Gracias a el, CalculadoraPrecio no necesita un
 * "if (politica == null)": siempre tiene una politica valida que aplicar.
 */
public class SinDescuento implements PoliticaDescuento {

    @Override
    public TipoCliente tipoCliente() {
        return TipoCliente.ESTANDAR;
    }

    @Override
    public double aplicar(double monto) {
        return monto;
    }
}
