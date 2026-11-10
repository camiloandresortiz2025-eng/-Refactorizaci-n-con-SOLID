/**
 * Pasarela Nequi. Encapsula la rama:
 *
 *     else if (metodoPago.equals("NEQUI")) { ... }
 *
 * Es el caso del escenario 3.1: con este diseno, agregar Nequi habria sido
 * CREAR esta clase, sin tocar 12 clases existentes.
 */
public class PagoNequi implements ProcesadorPago {

    @Override
    public MetodoPago metodoSoportado() {
        return MetodoPago.NEQUI;
    }

    @Override
    public ResultadoPago procesar(double monto) {
        if (monto <= 0) {
            return ResultadoPago.fallido(monto, "El monto a cobrar debe ser mayor que cero.");
        }
        System.out.printf("Conectando con API de Nequi... Procesando $%.2f%n", monto);
        return ResultadoPago.exitoso(monto,
                String.format("Su pago de $%.2f por Nequi fue exitoso.", monto));
    }

    @Override
    public String toString() {
        return "PagoNequi";
    }
}
