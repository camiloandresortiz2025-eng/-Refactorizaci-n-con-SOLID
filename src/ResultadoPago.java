/**
 * Resultado explicito de un intento de cobro.
 *
 * Corrige el defecto del metodo legacy, que devolvia void: cuando el pago no se
 * podia procesar imprimia un mensaje en consola y hacia "return", de modo que
 * quien lo llamaba creia que el cobro habia salido bien. Ahora el llamador
 * recibe el resultado y puede decidir que hacer.
 *
 * Es parte del contrato que respetan TODAS las implementaciones de
 * ProcesadorPago (LSP): ninguna devuelve null.
 */
public class ResultadoPago {

    private final boolean exitoso;
    private final double monto;
    private final String mensaje;

    private ResultadoPago(boolean exitoso, double monto, String mensaje) {
        this.exitoso = exitoso;
        this.monto = monto;
        this.mensaje = mensaje;
    }

    public static ResultadoPago exitoso(double monto, String mensaje) {
        return new ResultadoPago(true, monto, mensaje);
    }

    public static ResultadoPago fallido(double monto, String mensaje) {
        return new ResultadoPago(false, monto, mensaje);
    }

    public boolean esExitoso() {
        return exitoso;
    }

    public double getMonto() {
        return monto;
    }

    public String getMensaje() {
        return mensaje;
    }

    @Override
    public String toString() {
        return (exitoso ? "[OK] " : "[FALLO] ") + mensaje;
    }
}
