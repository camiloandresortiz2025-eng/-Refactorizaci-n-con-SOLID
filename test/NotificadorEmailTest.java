import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * El canal de notificacion es ahora una pieza independiente y probable por si
 * sola (SRP + ISP). En el legacy esta logica estaba enterrada dentro del metodo
 * de pago y no habia forma de ejercitarla aparte.
 */
@DisplayName("NotificadorEmail")
class NotificadorEmailTest {

    private final Notificador notificador = new NotificadorEmail();

    @Test
    @DisplayName("envia la notificacion a un destinatario valido")
    void notificaDestinatarioValido() {
        assertDoesNotThrow(() -> notificador.notificar("cliente@correo.com", "Pago exitoso"));
    }

    @Test
    @DisplayName("ante un destinatario invalido no interrumpe el flujo de la venta")
    void noExplotaConDestinatarioInvalido() {
        assertDoesNotThrow(() -> notificador.notificar(null, "Pago exitoso"));
        assertDoesNotThrow(() -> notificador.notificar("correo-sin-arroba", "Pago exitoso"));
    }
}
