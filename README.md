# Mobs de la Ruleta Naranja (Fabric 1.21.1)

Mod de Fabric para Minecraft 1.21.1. Se instala aparte de los mods de ruleta de dificultad (otro id: `mobsruleta`) y es compatible con ellos.

- **Ruleta naranja**: clic derecho abre el menú transparente (rueda naranja, números en 4 columnas, recuadro con el mob y los botones ACTIVAR / DESACTIVAR). Solo la usan admins (operador nivel 2+). Activar/desactivar no manda mensajes. ACTIVAR = el mob puede aparecer en el mundo, DESACTIVAR = deja de aparecer (los que ya existen se quedan). Por defecto todos están activados.
- Para obtenerla: `/give @s mobsruleta:ruleta_naranja`
- Los 10 mobs son hostiles, **no sueltan loot** (loot tables vacías), tienen **huevo de aparición** (pestaña de huevos del creativo) y **aparecen naturalmente en el overworld**.
- Requiere Fabric Loader 0.16+ y Fabric API, en el servidor y en los clientes.

## Mobs

| N.º | Mob | Qué hace | Dónde aparece |
|----:|-----|----------|---------------|
| 1 | Creeper nevado | Al explotar te congela: filtro azul y hay que presionar espacio 200 veces | Biomas fríos/nevados |
| 2 | Creeper de pasto | Explota con el doble de fuerza (6.0 vs 3.0) | Al romper pasto, musgo o helechos (35%) y, raro, de forma natural |
| 3 | Creeper de resorte | Al explotar te lanza unos 25 bloques hacia arriba (sin daño de explosión) y caes | Todos los biomas, mucho más en pantanos |
| 4 | Fantasma de la mente | Atraviesa paredes; daño + ceguera + inmóvil 7 s | Cerca de jugadores con más de 4 días sin dormir |
| 5 | Dragón de aire | Te agarra en picada y te sube más de 24 bloques; luego te suelta | En el cielo, sobre jugadores al aire libre |
| 6 | Ojo de la demencia | Tu cámara queda fija en él y recibes daño hasta que un bloque se interponga | Pegado en paredes y techos |
| 7 | Creeper de lava | Explosión con fuego, te prende en llamas | Oscuridad |
| 8 | Creeper venenoso | Deja una nube de veneno | Oscuridad |
| 9 | Acechador | Alto, rápido, golpea muy fuerte | Oscuridad |
| 10 | Gólem de lodo | Lento y resistente, sus golpes ralentizan | Oscuridad, más en pantanos |

Los mobs terrestres aparecen cada ~5 s cerca de los jugadores, en lugares oscuros (luz 7 o menos), con un límite por zona.

## Ajustes rápidos (en el código)

- Fuerza del lanzamiento del creeper de resorte: `SpringCreeperEntity.VELOCIDAD_SALTO`
- Pulsaciones para descongelarse: `SnowCreeperEntity.PULSACIONES`
- Altura que sube el dragón: `AirDragonEntity.ALTURA_SUBIDA`
- Probabilidades de aparición: `Spawner.java`
- Textos y descripciones del menú: `Mobs.java`
- Los modelos y texturas están en `assets/mobsruleta/mobdefs/mobs.json` y `textures/entity/`

## Descargar el .jar

Al subir el código a la rama `main`, GitHub Actions compila el mod y publica el `.jar` en **Releases** (etiqueta `latest`).
