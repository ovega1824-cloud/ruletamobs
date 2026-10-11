package com.mobsruleta.entity;

import com.mobsruleta.Efectos;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Creeper de resorte: al explotar lanza a los jugadores cercanos unos 25 bloques hacia arriba (sin daño de explosión). */
public class SpringCreeperEntity extends CreeperBase {

    /** Velocidad vertical que sube unos 25 bloques. */
    public static final double VELOCIDAD_SALTO = 2.3;

    public SpringCreeperEntity(EntityType<? extends SpringCreeperEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return atributosCreeper(20.0, 0.25);
    }

    @Override
    protected void explotar(ServerWorld mundo) {
        sonidoExplosion(mundo);
        mundo.spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY() + 0.5, this.getZ(), 6, 0.6, 0.3, 0.6, 0.0);
        for (ServerPlayerEntity jugador : mundo.getPlayers(p -> p.squaredDistanceTo(this) < 36.0 && !p.isSpectator())) {
            Efectos.lanzar(jugador, VELOCIDAD_SALTO);
        }
    }
}
