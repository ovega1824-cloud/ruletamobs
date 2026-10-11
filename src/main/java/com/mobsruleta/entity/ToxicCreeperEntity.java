package com.mobsruleta.entity;

import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Creeper venenoso: al explotar deja una nube de veneno. */
public class ToxicCreeperEntity extends CreeperBase {

    public ToxicCreeperEntity(EntityType<? extends ToxicCreeperEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return atributosCreeper(20.0, 0.25);
    }

    @Override
    protected void explotar(ServerWorld mundo) {
        mundo.createExplosion(this, this.getX(), this.getY(), this.getZ(), 2.5f, World.ExplosionSourceType.MOB);
        AreaEffectCloudEntity nube = new AreaEffectCloudEntity(mundo, this.getX(), this.getY(), this.getZ());
        nube.setRadius(3.5f);
        nube.setRadiusOnUse(-0.3f);
        nube.setWaitTime(10);
        nube.setDuration(300);
        nube.addEffect(new StatusEffectInstance(StatusEffects.POISON, 200, 1));
        mundo.spawnEntity(nube);
    }
}
