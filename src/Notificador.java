/**
 * SRP + ISP: la notificacion es una responsabilidad aparte del cobro.
 *
 * En el legacy el envio del correo estaba dentro del metodo de pago (de ahi la
 * "Y" en procesarPagoYEnviarNotificacion). Con esta interfaz, cambiar de correo
 * a SMS o WhatsApp es escribir otra implementacion, sin abrir la logica de pagos.
 *
 * Es una interfaz de un solo metodo a proposito: quien solo necesita avisar al
 * cliente no debe depender de nada mas.
 */
public interface Notificador {

    void notificar(String destino, String mensaje);
}
