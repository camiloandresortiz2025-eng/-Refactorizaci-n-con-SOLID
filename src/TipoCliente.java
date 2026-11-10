/**
 * Reemplaza el parametro String tipoCliente del codigo legacy.
 *
 * Con un String, los valores "vip", "Vip" o " VIP " caian silenciosamente en el
 * caso sin descuento y el error solo se detectaba en produccion (o cuando el
 * cliente reclamaba). Con un enum, un valor invalido no compila.
 */
public enum TipoCliente {
    ESTANDAR,
    VIP,
    MAYORISTA
}
