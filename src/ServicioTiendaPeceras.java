import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de dominio desacoplado: el reemplazo de TiendaPecerasLegacy.
 *
 * Esta clase COORDINA, no ejecuta detalles:
 *  - SRP: no construye SQL, no calcula impuestos, no habla con pasarelas y no
 *    envia correos. Delega cada cosa a su responsable.
 *  - DIP: todas sus dependencias son interfaces y llegan por el constructor
 *    (inyeccion de dependencias). La composicion se hace en Main.
 *  - OCP: nuevos medios de pago, descuentos o canales de notificacion se
 *    agregan desde afuera; esta clase no se modifica.
 */
public class ServicioTiendaPeceras {

    private final PeceraRepository repositorio;
    private final CalculadoraPrecio calculadoraPrecio;
    private final Notificador notificador;
    private final Map<MetodoPago, ProcesadorPago> procesadores = new EnumMap<>(MetodoPago.class);

    public ServicioTiendaPeceras(PeceraRepository repositorio,
                                 CalculadoraPrecio calculadoraPrecio,
                                 Notificador notificador,
                                 List<ProcesadorPago> procesadoresDisponibles) {
        if (repositorio == null || calculadoraPrecio == null || notificador == null) {
            throw new IllegalArgumentException("Todas las dependencias del servicio son obligatorias.");
        }
        this.repositorio = repositorio;
        this.calculadoraPrecio = calculadoraPrecio;
        this.notificador = notificador;
        if (procesadoresDisponibles != null) {
            for (ProcesadorPago procesador : procesadoresDisponibles) {
                this.procesadores.put(procesador.metodoSoportado(), procesador);
            }
        }
    }

    /**
     * Antes: agregarPecera(String, double) -> armaba un String y escribia el
     * INSERT de MySQL en el mismo metodo.
     * Ahora: solo crea la entidad y delega la persistencia.
     */
    public Pecera agregarPecera(String tipo, double precio) {
        Pecera pecera = new Pecera(tipo, precio);
        repositorio.guardar(pecera);
        return pecera;
    }

    public List<Pecera> consultarInventario() {
        return repositorio.listarTodas();
    }

    /**
     * Antes: calcularTotalConImpuesto(double, String) con if / else if.
     * Ahora: delega a CalculadoraPrecio, que resuelve por polimorfismo.
     */
    public double cotizar(double precioBase, TipoCliente tipoCliente) {
        return calculadoraPrecio.calcularTotal(precioBase, tipoCliente);
    }

    /**
     * Antes: procesarPagoYEnviarNotificacion(...) hacia dos cosas, tenia un
     * if / else if por medio de pago, conocia las pasarelas y el SMTP, y
     * devolvia void (un pago fallido pasaba inadvertido).
     *
     * Ahora: resuelve la pasarela por polimorfismo, delega la notificacion y
     * devuelve el resultado al llamador.
     */
    public ResultadoPago procesarVenta(MetodoPago metodoPago, double monto, String emailCliente) {
        ProcesadorPago procesador = procesadores.get(metodoPago);
        if (procesador == null) {
            throw new MetodoPagoNoSoportadoException(metodoPago);
        }

        ResultadoPago resultado = procesador.procesar(monto);

        if (resultado.esExitoso()) {
            notificador.notificar(emailCliente, resultado.getMensaje());
        }
        return resultado;
    }

    /** Flujo completo de venta: cotiza, cobra y notifica. */
    public ResultadoPago venderPecera(Pecera pecera,
                                      TipoCliente tipoCliente,
                                      MetodoPago metodoPago,
                                      String emailCliente) {
        double total = cotizar(pecera.getPrecio(), tipoCliente);
        return procesarVenta(metodoPago, total, emailCliente);
    }
}
