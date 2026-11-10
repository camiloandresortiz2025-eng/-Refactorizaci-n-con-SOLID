/**
 * OCP mediante el patron Strategy.
 *
 * Reemplaza la cadena if / else if que decidia el descuento segun el
 * tipoCliente. Cada politica es una clase independiente; agregar una nueva no
 * obliga a modificar CalculadoraPrecio.
 *
 * Contrato (LSP) que toda implementacion debe respetar:
 *  - aplicar() recibe un monto >= 0 y devuelve un monto >= 0.
 *  - nunca lanza excepciones de negocio ni devuelve valores negativos.
 *  - tipoCliente() devuelve siempre el mismo valor para la misma instancia.
 */
public interface PoliticaDescuento {

    /** Tipo de cliente al que corresponde esta politica. */
    TipoCliente tipoCliente();

    /** Devuelve el monto con el descuento aplicado. */
    double aplicar(double monto);
}
