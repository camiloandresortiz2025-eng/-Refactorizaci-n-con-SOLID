/**
 * Canal de notificacion por correo (SMTP).
 *
 * El detalle del protocolo queda encerrado en esta clase; el servicio de
 * dominio solo conoce la interfaz Notificador.
 */
public class NotificadorEmail implements Notificador {

    @Override
    public void notificar(String destino, String mensaje) {
        if (destino == null || !destino.contains("@")) {
            System.out.println("[SMTP] Correo no enviado: destinatario invalido.");
            return;
        }
        System.out.printf("Enviando correo SMTP a %s: %s%n", destino, mensaje);
    }
}
