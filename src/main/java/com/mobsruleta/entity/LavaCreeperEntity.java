package com.mobsruleta.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Creeper de lava: explota con fuego y prende en llamas a los jugadores cercanos. */
public class LavaCreeperEntity extends CreeperBase {

    public LavaCreeperEntity(EntityType<? extends LavaCreeperEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return atributosCreeper(24.0, 0.26);
    }

    @Override
    protected void explotar(ServerWorld mundo) {
        mundo.createExplosion(this, this.getX(), this.getY(), this.getZ(), 3.5f, true, World.ExplosionSourceType.MOB);
        mundo.spawnParticles(ParticleTypes.LAVA, this.getX(), this.getY() + 1.0, this.getZ(), 40, 1.0, 0.8, 1.0, 0.1);
        for (ServerPlayerEntity jugador : mundo.getPlayers(p -> p.squaredDistanceTo(this) < 25.0 && !p.isSpectator())) {
            jugador.setOnFireFor(8);
        }
    }
}
