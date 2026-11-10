/**
 * Pasarela de pago simulada para pruebas.
 *
 * Permite forzar un cobro exitoso o fallido sin conectarse a ninguna pasarela
 * real. Respeta el mismo contrato que PagoTarjeta y PagoNequi, por lo que el
 * servicio la acepta sin cambios: eso es el LSP en la practica.
 */
public class PagoSimulado implements ProcesadorPago {

    private final MetodoPago metodo;
    private final boolean debeSerExitoso;
    private int vecesInvocado = 0;

    public PagoSimulado(MetodoPago metodo, boolean debeSerExitoso) {
        this.metodo = metodo;
        this.debeSerExitoso = debeSerExitoso;
    }

    @Override
    public MetodoPago metodoSoportado() {
        return metodo;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        vecesInvocado++;
        // El doble debe respetar el MISMO contrato que las pasarelas reales
        // (LSP): un monto invalido se rechaza siempre, sin importar como se
        // haya configurado la simulacion.
        if (monto <= 0) {
            return ResultadoPago.fallido(monto, "El monto a cobrar debe ser mayor que cero.");
        }
        if (!debeSerExitoso) {
            return ResultadoPago.fallido(monto, "Fondos insuficientes (simulado).");
        }
        return ResultadoPago.exitoso(monto, String.format("Pago simulado de $%.2f aprobado.", monto));
    }

    public int vecesInvocado() {
        return vecesInvocado;
    }

    @Override
    public String toString() {
        return "PagoSimulado(" + metodo + ")";
    }
}
