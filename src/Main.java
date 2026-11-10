import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Raiz de composicion (Composition Root) y demostracion.
 *
 * Este es el unico lugar del programa que conoce las clases concretas: aqui se
 * arma el grafo de dependencias y se inyecta al servicio. Todo lo demas trabaja
 * contra interfaces.
 */
public class Main {

    public static void main(String[] args) {

        // ---------- Composicion de dependencias (DIP) ----------
        PeceraRepository repositorio = new RepositorioPecerasMySql();

        PoliticaImpuesto iva = new ImpuestoIvaColombia(ImpuestoIvaColombia.TARIFA_GENERAL);

        List<PoliticaDescuento> descuentos = Arrays.asList(
                new SinDescuento(),
                new DescuentoVip(),
                new DescuentoMayorista());

        CalculadoraPrecio calculadora = new CalculadoraPrecio(iva, descuentos);

        Notificador notificador = new NotificadorEmail();

        List<ProcesadorPago> pasarelas = new ArrayList<>();
        pasarelas.add(new PagoTarjeta());
        pasarelas.add(new PagoNequi());

        ServicioTiendaPeceras tienda =
                new ServicioTiendaPeceras(repositorio, calculadora, notificador, pasarelas);

        // ---------- 1. Inventario (antes: SRP + DIP violados) ----------
        System.out.println("=== 1. Registro de inventario ===");
        Pecera nano = tienda.agregarPecera("Pecera Nano 20L", 150000);
        Pecera plantada = tienda.agregarPecera("Pecera Plantada 120L", 890000);
        System.out.println("Inventario actual: " + tienda.consultarInventario());

        // ---------- 2. Calculo de precios (antes: OCP violado) ----------
        System.out.println("\n=== 2. Cotizaciones por tipo de cliente ===");
        for (TipoCliente tipo : TipoCliente.values()) {
            System.out.printf("%-10s -> total: $%.2f%n",
                    tipo, tienda.cotizar(nano.getPrecio(), tipo));
        }

        // ---------- 3. Venta completa (antes: SRP + OCP + DIP violados) ----------
        System.out.println("\n=== 3. Venta con tarjeta a cliente VIP ===");
        ResultadoPago r1 = tienda.venderPecera(
                plantada, TipoCliente.VIP, MetodoPago.TARJETA, "cliente.vip@correo.com");
        System.out.println("Resultado: " + r1);

        System.out.println("\n=== 4. Venta por Nequi a mayorista ===");
        ResultadoPago r2 = tienda.venderPecera(
                plantada, TipoCliente.MAYORISTA, MetodoPago.NEQUI, "mayorista@acuarios.com");
        System.out.println("Resultado: " + r2);

        // ---------- 5. Fallo explicito (antes: println silencioso) ----------
        System.out.println("\n=== 5. Metodo de pago sin pasarela registrada ===");
        try {
            tienda.procesarVenta(MetodoPago.PSE, 100000, "cliente@correo.com");
        } catch (MetodoPagoNoSoportadoException e) {
            System.out.println("Error controlado: " + e.getMessage());
        }

        // ---------- 6. Demostracion del OCP ----------
        // Se agrega una pasarela NUEVA sin modificar ninguna clase ya escrita.
        System.out.println("\n=== 6. Extension: se agrega PSE sin tocar el servicio ===");
        List<ProcesadorPago> pasarelasAmpliadas = new ArrayList<>(pasarelas);
        pasarelasAmpliadas.add(new PagoPse());

        ServicioTiendaPeceras tiendaAmpliada =
                new ServicioTiendaPeceras(repositorio, calculadora, notificador, pasarelasAmpliadas);

        ResultadoPago r3 = tiendaAmpliada.procesarVenta(
                MetodoPago.PSE, 100000, "cliente@correo.com");
        System.out.println("Resultado: " + r3);

        // ---------- 7. Testabilidad lograda con el DIP ----------
        System.out.println("\n=== 7. El mismo servicio con repositorio en memoria (pruebas) ===");
        ServicioTiendaPeceras tiendaDePrueba = new ServicioTiendaPeceras(
                new RepositorioPecerasEnMemoria(), calculadora, notificador, pasarelas);
        tiendaDePrueba.agregarPecera("Pecera de prueba", 1000);
        System.out.println("Registros en memoria (sin tocar MySQL): "
                + tiendaDePrueba.consultarInventario().size());
    }

    /**
     * Nueva pasarela agregada para demostrar el OCP: ninguna clase existente
     * tuvo que modificarse para soportarla.
     */
    static class PagoPse implements ProcesadorPago {

        @Override
        public MetodoPago metodoSoportado() {
            return MetodoPago.PSE;
        }

        @Override
        public ResultadoPago procesar(double monto) {
            if (monto <= 0) {
                return ResultadoPago.fallido(monto, "El monto a cobrar debe ser mayor que cero.");
            }
            System.out.printf("Redirigiendo al portal PSE del banco... Procesando $%.2f%n", monto);
            return ResultadoPago.exitoso(monto,
                    String.format("Su pago de $%.2f por PSE fue exitoso.", monto));
        }
    }
}
