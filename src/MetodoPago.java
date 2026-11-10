/**
 * Reemplaza el parametro String metodoPago del codigo legacy.
 *
 * Nota de diseno: agregar un valor aqui NO obliga a modificar la logica de
 * negocio. El enum solo nombra el medio; quien sabe COMO cobrar es cada
 * implementacion de ProcesadorPago, registrada en el arranque (ver Main).
 */
public enum MetodoPago {
    TARJETA,
    NEQUI,
    PSE
}
