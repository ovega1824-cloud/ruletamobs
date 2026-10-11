package com.mobsruleta.entity;

import com.mobsruleta.Efectos;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Creeper nevado: al explotar congela a los jugadores cercanos (filtro azul y 200 pulsaciones de espacio). */
public class SnowCreeperEntity extends CreeperBase {

    public static final int PULSACIONES = 200;

    public SnowCreeperEntity(EntityType<? extends SnowCreeperEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return atributosCreeper(20.0, 0.25);
    }

    @Override
    protected void explotar(ServerWorld mundo) {
        mundo.createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.0f, World.ExplosionSourceType.NONE);
        mundo.spawnParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY() + 1.0, this.getZ(), 80, 1.5, 1.0, 1.5, 0.1);
        for (ServerPlayerEntity jugador : mundo.getPlayers(p -> p.squaredDistanceTo(this) < 36.0 && !p.isSpectator())) {
            Efectos.congelar(jugador, PULSACIONES);
        }
    }
}
