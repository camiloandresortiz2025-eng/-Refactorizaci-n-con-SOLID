import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementacion de persistencia para MySQL.
 *
 * Todo el conocimiento del motor de base de datos (el dialecto SQL) quedo
 * encerrado aqui. Si manana se migra a PostgreSQL se escribe otra clase y el
 * negocio no se toca.
 *
 * Nota: se mantiene el System.out.println del ejercicio original porque el
 * taller simula el acceso a datos; en un proyecto real aqui iria JDBC/JPA.
 */
public class RepositorioPecerasMySql implements PeceraRepository {

    private final List<Pecera> almacen = new ArrayList<>();

    @Override
    public void guardar(Pecera pecera) {
        almacen.add(pecera);
        System.out.printf(
                "[DB MySQL] INSERT INTO peceras (tipo, precio) VALUES ('%s', %.2f)%n",
                pecera.getTipo(), pecera.getPrecio());
    }

    @Override
    public List<Pecera> listarTodas() {
        return Collections.unmodifiableList(new ArrayList<>(almacen));
    }
}
