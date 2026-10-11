package com.mobsruleta;

import java.util.List;

/** Lista de los 15 mobs. El número del menú de la ruleta (1 a 15) es la posición en esta lista. */
public final class Mobs {

    /** egg1 y egg2 son los colores del huevo de aparición. */
    public record Def(String id, String nombre, String descripcion, int egg1, int egg2) {
    }

    public static final List<Def> ALL = List.of(
            new Def("snow_creeper", "Creeper nevado", "al explotar te congela: filtro azul y tienes que presionar espacio 200 veces para descongelarte. Aparece en biomas nevados", 0x78D7E6, 0x1B6F8A),
            new Def("grass_creeper", "Creeper de pasto", "aparece al romper pasto o musgo y explota más fuerte que un creeper normal", 0x3CAA1E, 0x0F4F08),
            new Def("spring_creeper", "Creeper de resorte", "al explotar te lanza unos 25 bloques hacia arriba y caes en picada. Aparece en todos los biomas, más en pantanos", 0x262626, 0x9A9A9A),
            new Def("mind_ghost", "Fantasma de la mente", "aparece si pasas más de 4 días sin dormir, atraviesa paredes y te deja sin ver ni moverte 7 segundos", 0xF0F0F0, 0xC80000),
            new Def("air_dragon", "Dragón de aire", "vive en el cielo; si lo miras te agarra y te sube más de 24 bloques", 0xCDE1F0, 0x4A8CD0),
            new Def("dementia_eye", "Ojo de la demencia", "pegado en paredes y techos; si lo ves tu cámara se queda fija en él y recibes daño hasta que algo se interponga", 0xEBE1E1, 0x960000),
            new Def("lava_creeper", "Creeper de lava", "explota con fuego y te prende en llamas", 0x461E14, 0xFF9600),
            new Def("toxic_creeper", "Creeper venenoso", "al explotar deja una nube de veneno", 0x783C96, 0x96FF3C),
            new Def("stalker", "Acechador", "alto y rápido, golpea muy fuerte", 0x231E2D, 0xFFFFFF),
            new Def("mud_golem", "Gólem de lodo", "lento y resistente, sus golpes te ralentizan", 0x6E4E30, 0xFADC50)
    );

    private Mobs() {
    }

    /** Posición en el menú (1 a 15) o 0 si no existe. */
    public static int numero(String id) {
        for (int i = 0; i < ALL.size(); i++) {
            if (ALL.get(i).id().equals(id)) {
                return i + 1;
            }
        }
        return 0;
    }

    public static Def obtener(int numero) {
        if (numero < 1 || numero > ALL.size()) {
            return null;
        }
        return ALL.get(numero - 1);
    }
}
