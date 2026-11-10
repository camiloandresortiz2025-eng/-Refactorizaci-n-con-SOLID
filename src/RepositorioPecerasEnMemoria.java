import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementacion en memoria, pensada para pruebas automatizadas.
 *
 * Es la prueba practica de que el DIP funciono: con el codigo legacy era
 * imposible probar la tienda sin tocar la base de datos. Ahora el test le
 * inyecta esta clase al servicio y corre en milisegundos, sin MySQL.
 *
 * LSP: respeta exactamente el mismo contrato que RepositorioPecerasMySql, por
 * eso el servicio no necesita saber cual de las dos recibio.
 */
public class RepositorioPecerasEnMemoria implements PeceraRepository {

    private final List<Pecera> almacen = new ArrayList<>();

    @Override
    public void guardar(Pecera pecera) {
        almacen.add(pecera);
    }

    @Override
    public List<Pecera> listarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(almacen));
    }
}
