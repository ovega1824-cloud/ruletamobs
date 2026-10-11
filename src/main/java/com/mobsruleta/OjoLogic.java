package com.mobsruleta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mobsruleta.entity.DementiaEyeEntity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/**
 * Ojos de la demencia: cuando un jugador ve uno, su cámara queda fija en él y recibe daño
 * hasta que algún bloque se interponga entre el ojo y su vista.
 */
public final class OjoLogic {

    private static final double ALCANCE = 32.0;
    private static final Map<UUID, Integer> BLOQUEADO = new HashMap<>();

    private OjoLogic() {
    }

    public static void tick(MinecraftServer server) {
        for (ServerPlayerEntity jugador : server.getPlayerManager().getPlayerList()) {
            ServerWorld mundo = jugador.getServerWorld();
            UUID id = jugador.getUuid();
            DementiaEyeEntity ojo = null;

            Integer actual = BLOQUEADO.get(id);
            if (actual != null) {
                Entity e = mundo.getEntityById(actual);
                if (e instanceof DementiaEyeEntity d && d.isAlive() && jugador.isAlive() && !jugador.isSpectator()
                        && d.squaredDistanceTo(jugador) <= (ALCANCE + 8) * (ALCANCE + 8) && visible(jugador, d)) {
                    ojo = d;
                } else {
                    BLOQUEADO.remove(id);
                    ServerPlayNetworking.send(jugador, new LockPayload(-1));
                }
            }

            if (ojo == null && jugador.isAlive() && !jugador.isSpectator()) {
                for (DementiaEyeEntity d : mundo.getEntitiesByClass(DementiaEyeEntity.class,
                        jugador.getBoundingBox().expand(ALCANCE), Entity::isAlive)) {
                    if (d.squaredDistanceTo(jugador) > ALCANCE * ALCANCE) {
                        continue;
                    }
                    Vec3d haciaOjo = centro(d).subtract(jugador.getEyePos()).normalize();
                    if (jugador.getRotationVec(1.0f).dotProduct(haciaOjo) > 0.5 && visible(jugador, d)) {
                        ojo = d;
                        BLOQUEADO.put(id, d.getId());
                        ServerPlayNetworking.send(jugador, new LockPayload(d.getId()));
                        break;
                    }
                }
            }

            if (ojo != null && jugador.age % 20 == 0 && !jugador.isCreative()) {
                jugador.damage(mundo.getDamageSources().magic(), 2.0f);
            }
        }
    }

    public static Vec3d centro(Entity ojo) {
        return new Vec3d(ojo.getX(), ojo.getY() + 0.3, ojo.getZ());
    }

    /** true si ningún bloque se interpone entre los ojos del jugador y el ojo de la demencia. */
    private static boolean visible(ServerPlayerEntity jugador, DementiaEyeEntity ojo) {
        Vec3d desde = jugador.getEyePos();
        Vec3d hasta = centro(ojo);
        return jugador.getWorld().raycast(new RaycastContext(desde, hasta,
                RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, jugador)).getType() == HitResult.Type.MISS;
    }
}
