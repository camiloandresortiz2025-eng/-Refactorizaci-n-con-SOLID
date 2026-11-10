/**
 * Pasarela de tarjeta. Encapsula la rama:
 *
 *     if (metodoPago.equals("TARJETA")) { ... }
 *
 * Todo el detalle de integracion con la pasarela vive aqui y en ningun otro
 * lado del sistema.
 */
public class PagoTarjeta implements ProcesadorPago {

    @Override
    public MetodoPago metodoSoportado() {
        return MetodoPago.TARJETA;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        if (monto <= 0) {
            return ResultadoPago.fallido(monto, "El monto a cobrar debe ser mayor que cero.");
        }
        System.out.printf("Conectando con pasarela de pago con tarjeta... Procesando $%.2f%n", monto);
        return ResultadoPago.exitoso(monto,
                String.format("Su pago de $%.2f con tarjeta fue exitoso.", monto));
    }

    @Override
    public String toString() {
        return "PagoTarjeta";
    }
}
