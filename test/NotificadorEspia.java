import java.util.ArrayList;
import java.util.List;

/**
 * Test double (espia) del canal de notificacion.
 *
 * Esta clase es, por si sola, la demostracion de que el DIP resolvio el
 * problema del codigo legacy: alli el envio del correo estaba incrustado dentro
 * del metodo de pago, asi que probar una venta implicaba enviar un correo real.
 * Ahora el servicio depende de la interfaz Notificador y el test le inyecta
 * esta implementacion, que en vez de enviar nada se limita a registrar lo que
 * se le pidio enviar.
 */
public class NotificadorEspia implements Notificador {

    private final List<String> destinos = new ArrayList<>();
    private final List<String> mensajes = new ArrayList<>();

    @Override
    public void notificar(String destino, String mensaje) {
        destinos.add(destino);
        mensajes.add(mensaje);
    }

    public int cantidadDeNotificaciones() {
        return destinos.size();
    }

    public String ultimoDestino() {
        return destinos.isEmpty() ? null : destinos.get(destinos.size() - 1);
    }

    public String ultimoMensaje() {
        return mensajes.isEmpty() ? null : mensajes.get(mensajes.size() - 1);
    }
}
