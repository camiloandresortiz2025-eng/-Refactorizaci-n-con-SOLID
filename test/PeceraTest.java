import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Entidad Pecera")
class PeceraTest {

    @Test
    @DisplayName("conserva tipo y precio como datos, no como texto concatenado")
    void conservaLosDatosDelDominio() {
        Pecera pecera = new Pecera("Pecera Nano 20L", 150000);

        assertEquals("Pecera Nano 20L", pecera.getTipo());
        assertEquals(150000, pecera.getPrecio(), 0.001);
    }

    @Test
    @DisplayName("recorta espacios sobrantes en el tipo")
    void recortaEspacios() {
        assertEquals("Pecera Marina", new Pecera("  Pecera Marina  ", 1000).getTipo());
    }

    @Test
    @DisplayName("rechaza un tipo vacio o nulo")
    void rechazaTipoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Pecera(null, 1000));
        assertThrows(IllegalArgumentException.class, () -> new Pecera("   ", 1000));
    }

    @Test
    @DisplayName("rechaza un precio negativo")
    void rechazaPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () -> new Pecera("Pecera Nano", -1));
    }
}
