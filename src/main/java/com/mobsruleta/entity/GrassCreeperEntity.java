package com.mobsruleta.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/** Creeper de pasto: aparece al romper pasto o musgo y explota con el doble de fuerza que uno normal. */
public class GrassCreeperEntity extends CreeperBase {

    /** Un creeper normal explota con 3.0. */
    public static final float POTENCIA = 6.0f;

    public GrassCreeperEntity(EntityType<? extends GrassCreeperEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return atributosCreeper(20.0, 0.26);
    }

    @Override
    protected void explotar(ServerWorld mundo) {
        mundo.createExplosion(this, this.getX(), this.getY(), this.getZ(), POTENCIA, World.ExplosionSourceType.MOB);
    }
}
