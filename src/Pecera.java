/**
 * Entidad de dominio.
 *
 * En el codigo legacy el inventario era un List<String> con la informacion
 * concatenada ("Pecera Nano - $150.0"), lo que destruia los datos: no se podia
 * ordenar por precio ni filtrar por tipo sin partir cadenas de texto.
 *
 * Ahora el dominio tiene su propio objeto, inmutable y validado.
 */
public class Pecera {

    private final String tipo;
    private final double precio;

    public Pecera(String tipo, double precio) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de pecera es obligatorio.");
        }
        if (precio < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo.");
        }
        this.tipo = tipo.trim();
        this.precio = precio;
    }

    public String getTipo() {
        return tipo;
    }

    public double getPrecio() {
        return precio;
    }

    @Override
    public String toString() {
        return String.format("Pecera{tipo='%s', precio=%.2f}", tipo, precio);
    }
}
