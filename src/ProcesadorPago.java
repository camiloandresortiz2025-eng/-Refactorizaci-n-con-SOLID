/**
 * OCP + DIP: abstraccion de la pasarela de pago.
 *
 * Reemplaza la cadena if / else if de procesarPagoYEnviarNotificacion(), que es
 * exactamente el defecto descrito en la actividad 3.1: agregar un medio de pago
 * obligaba a abrir y modificar la clase de negocio.
 *
 * Contrato que TODA implementacion debe respetar (LSP):
 *  - procesar() recibe un monto > 0.
 *  - SIEMPRE devuelve un ResultadoPago, nunca null y nunca un return silencioso.
 *  - un fallo de negocio (fondos, pasarela caida) se reporta con
 *    ResultadoPago.fallido(), no lanzando excepciones propias ni imprimiendo.
 *  - no puede endurecer la precondicion (p. ej. exigir montos mayores a cierto
 *    valor), porque el servicio que la usa no tiene forma de saberlo.
 */
public interface ProcesadorPago {

    /** Medio de pago que atiende esta implementacion. */
    MetodoPago metodoSoportado();

    /** Ejecuta el cobro y devuelve el resultado. */
    ResultadoPago procesar(double monto);
}
