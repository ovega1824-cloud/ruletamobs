package com.mobsruleta;

import java.util.concurrent.ThreadLocalRandom;

import com.mobsruleta.entity.DementiaEyeEntity;
import com.mobsruleta.entity.MindGhostEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

/**
 * Aparición natural de los 10 mobs en el overworld. Cada 5 segundos, para cada jugador, se intenta hacer aparecer
 * cada mob según sus reglas (bioma, oscuridad, cielo, días sin dormir, paredes...). Respeta el menú de la ruleta.
 */
public final class Spawner {

    /** 4 días de Minecraft sin dormir. */
    public static final int TICKS_SIN_DORMIR = 96000;

    private Spawner() {
    }

    public static void tick(MinecraftServer server) {
        if (server.getTicks() % 100 != 0) {
            return;
        }
        ServerWorld mundo = server.getOverworld();
        MobState estado = MobState.get(server);
        for (ServerPlayerEntity jugador : mundo.getPlayers()) {
            if (jugador.isSpectator() || !jugador.isAlive()) {
                continue;
            }
            RegistryEntry<Biome> bioma = mundo.getBiome(jugador.getBlockPos());
            boolean pantano = bioma.matchesKey(BiomeKeys.SWAMP) || bioma.matchesKey(BiomeKeys.MANGROVE_SWAMP);
            boolean frio = bioma.value().getTemperature() <= 0.15f;

            // Creepers y monstruos terrestres (necesitan oscuridad)
            if (frio) {
                terrestre(mundo, estado, jugador, "snow_creeper", 0.40, 4);
            }
            terrestre(mundo, estado, jugador, "spring_creeper", pantano ? 0.60 : 0.15, 3);
            terrestre(mundo, estado, jugador, "grass_creeper", 0.08, 2);
            terrestre(mundo, estado, jugador, "lava_creeper", 0.08, 2);
            terrestre(mundo, estado, jugador, "toxic_creeper", 0.10, 2);
            terrestre(mundo, estado, jugador, "stalker", 0.08, 2);
            terrestre(mundo, estado, jugador, "mud_golem", pantano ? 0.25 : 0.06, 2);

            // Fantasma de la mente: solo si el jugador lleva más de 4 días sin dormir
            if (activo(estado, "mind_ghost") && sinDormir(jugador) > TICKS_SIN_DORMIR
                    && contar(mundo, jugador, "mind_ghost") < 3 && azar() < 0.6) {
                BlockPos pos = jugador.getBlockPos().add(entre(-12, 12), entre(0, 4), entre(-12, 12));
                aparecer(mundo, "mind_ghost", pos);
            }

            // Dragón de aire: en el cielo, si el jugador está al aire libre
            if (activo(estado, "air_dragon") && contar(mundo, jugador, "air_dragon") < 2 && azar() < 0.10
                    && mundo.isSkyVisible(jugador.getBlockPos())) {
                BlockPos pos = jugador.getBlockPos().add(signo() * entre(20, 40), entre(26, 34), signo() * entre(20, 40));
                if (mundo.isChunkLoaded(pos) && mundo.isAir(pos)) {
                    aparecer(mundo, "air_dragon", pos);
                }
            }

            // Ojo de la demencia: pegado en paredes y techos
            if (activo(estado, "dementia_eye") && contar(mundo, jugador, "dementia_eye") < 4 && azar() < 0.30) {
                ojo(mundo, jugador);
            }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static double azar() {
        return ThreadLocalRandom.current().nextDouble();
    }

    private static int entre(int a, int b) {
        return a + ThreadLocalRandom.current().nextInt(b - a + 1);
    }

    private static int signo() {
        return ThreadLocalRandom.current().nextBoolean() ? 1 : -1;
    }

    private static boolean activo(MobState estado, String id) {
        return estado.isEnabled(Mobs.numero(id));
    }

    /** Ticks desde la última vez que el jugador durmió. */
    public static int sinDormir(PlayerEntity jugador) {
        if (jugador instanceof ServerPlayerEntity sp) {
            return sp.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST));
        }
        return 0;
    }

    private static int contar(ServerWorld mundo, ServerPlayerEntity jugador, String id) {
        EntityType<? extends HostileEntity> tipo = ModEntities.tipo(id);
        return mundo.getEntitiesByType(tipo, jugador.getBoundingBox().expand(80.0), e -> true).size();
    }

    private static MobEntity aparecer(ServerWorld mundo, String id, BlockPos pos) {
        EntityType<? extends HostileEntity> tipo = ModEntities.tipo(id);
        Entity e = tipo.spawn(mundo, pos, SpawnReason.NATURAL);
        return e instanceof MobEntity mob ? mob : null;
    }

    private static void terrestre(ServerWorld mundo, MobState estado, ServerPlayerEntity jugador,
                                  String id, double probabilidad, int limite) {
        if (!activo(estado, id) || azar() >= probabilidad || contar(mundo, jugador, id) >= limite) {
            return;
        }
        int x = jugador.getBlockX() + signo() * entre(22, 44);
        int z = jugador.getBlockZ() + signo() * entre(22, 44);
        if (!mundo.isChunkLoaded(new BlockPos(x, jugador.getBlockY(), z))) {
            return;
        }
        for (int y = jugador.getBlockY() + 12; y >= jugador.getBlockY() - 16; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos suelo = pos.down();
            if (mundo.isAir(pos) && mundo.isAir(pos.up()) && mundo.getBlockState(suelo).isSolidBlock(mundo, suelo)) {
                if (mundo.getLightLevel(pos) > 7 || !mundo.getWorldBorder().contains(pos)) {
                    return;
                }
                if (mundo.getClosestPlayer(x + 0.5, y, z + 0.5, 16.0, false) != null) {
                    return;
                }
                aparecer(mundo, id, pos);
                return;
            }
        }
    }

    /** Busca una pared o techo cerca del jugador y pone un ojo pegado a él. */
    private static void ojo(ServerWorld mundo, ServerPlayerEntity jugador) {
        for (int intento = 0; intento < 12; intento++) {
            BlockPos pos = jugador.getBlockPos().add(signo() * entre(6, 26), entre(-6, 8), signo() * entre(6, 26));
            if (!mundo.isChunkLoaded(pos) || !mundo.isAir(pos) || mundo.getLightLevel(pos) > 9) {
                continue;
            }
            Direction[] caras = {Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
            Direction pared = caras[ThreadLocalRandom.current().nextInt(caras.length)];
            BlockPos soporte = pos.offset(pared);
            if (!mundo.getBlockState(soporte).isSolidBlock(mundo, soporte)) {
                continue;
            }
            MobEntity mob = aparecer(mundo, "dementia_eye", pos);
            if (mob instanceof DementiaEyeEntity ojo) {
                ojo.pegarA(pared);
            }
            return;
        }
    }

    // ------------------------------------------------------------------ creeper de pasto

    /** Al romper pasto o musgo puede aparecer un creeper de pasto que ataca a quien lo rompió. */
    public static void alRomper(World mundo, PlayerEntity jugador, BlockPos pos, BlockState estado) {
        if (!(mundo instanceof ServerWorld sw) || jugador.isSpectator() || !MobState.activo(sw, "grass_creeper")) {
            return;
        }
        boolean pasto = estado.isOf(Blocks.GRASS_BLOCK) || estado.isOf(Blocks.MOSS_BLOCK) || estado.isOf(Blocks.MOSS_CARPET)
                || estado.isOf(Blocks.SHORT_GRASS) || estado.isOf(Blocks.TALL_GRASS)
                || estado.isOf(Blocks.FERN) || estado.isOf(Blocks.LARGE_FERN);
        if (!pasto || azar() >= 0.35) {
            return;
        }
        EntityType<? extends HostileEntity> tipo = ModEntities.tipo("grass_creeper");
        Entity e = tipo.spawn(sw, pos, SpawnReason.TRIGGERED);
        if (e instanceof MobEntity mob) {
            mob.setTarget(jugador);
        }
    }
}
