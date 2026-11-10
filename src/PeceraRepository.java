import java.util.List;

/**
 * DIP: abstraccion de persistencia.
 *
 * El codigo legacy escribia el INSERT de MySQL dentro del metodo de negocio
 * agregarPecera(), por lo que la clase de alto nivel dependia de un detalle de
 * bajo nivel. Con esta interfaz la dependencia se invierte: el negocio define
 * QUE necesita (guardar, listar) y la infraestructura decide COMO.
 *
 * ISP: solo dos metodos, los que el negocio realmente usa.
 */
public interface PeceraRepository {

    /** Persiste la pecera. Nunca recibe null. */
    void guardar(Pecera pecera);

    /** Devuelve el inventario completo. Nunca devuelve null (lista vacia si no hay datos). */
    List<Pecera> listarTodas();
}
