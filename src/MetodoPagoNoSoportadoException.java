/**
 * Sustituye al bloque:
 *
 *     } else {
 *         System.out.println("Metodo de pago no soportado.");
 *         return;
 *     }
 *
 * Un error de negocio no se reporta por consola: se informa al llamador.
 */
public class MetodoPagoNoSoportadoException extends RuntimeException {

    public MetodoPagoNoSoportadoException(MetodoPago metodoPago) {
        super("No hay una pasarela registrada para el metodo de pago: " + metodoPago);
    }
}
