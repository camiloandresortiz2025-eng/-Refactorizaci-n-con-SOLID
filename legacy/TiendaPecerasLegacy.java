import java.util.ArrayList;
import java.util.List;


public class TiendaPecerasLegacy {

    private List<String> inventarioPeceras = new ArrayList<>();

    
    public void agregarPecera(String tipoPecera, double precio) {
        inventarioPeceras.add(tipoPecera + " - $" + precio);
        
        
        System.out.println("[DB MySQL] INSERT INTO peceras (tipo, precio) VALUES ('" + tipoPecera + "', " + precio + ")");
    }

    
    public double calcularTotalConImpuesto(double precioBase, String tipoCliente) {
        double impuesto = 0.19; // IVA estándar
        
        
        if (tipoCliente.equals("VIP")) {
            return (precioBase * (1 + impuesto)) * 0.85; // 15% de descuento VIP
        } else if (tipoCliente.equals("MAYORISTA")) {
            return (precioBase * (1 + impuesto)) * 0.75; // 25% de descuento Mayorista
        }
        
        return precioBase * (1 + impuesto);
    }

    
    public void procesarPagoYEnviarNotificacion(String metodoPago, double monto, String emailCliente) {
        if (metodoPago.equals("TARJETA")) {
            System.out.println("Conectando con pasarela de pago con tarjeta... Procesando $" + monto);
        } else if (metodoPago.equals("NEQUI")) {
            System.out.println("Conectando con API de Nequi... Procesando $" + monto);
        } else {
            System.out.println("Método de pago no soportado.");
            return;
        }

        
        System.out.println("Enviando correo SMTP a " + emailCliente + ": Su pago de $" + monto + " fue exitoso.");
    }
}