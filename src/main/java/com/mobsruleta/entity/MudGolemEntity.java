package com.mobsruleta.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/** Gólem de lodo: lento y resistente; sus golpes ralentizan. */
public class MudGolemEntity extends MobBase {

    public MudGolemEntity(EntityType<? extends MudGolemEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 60.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.2)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 0.6)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 28.0);
    }

    @Override
    public boolean tryAttack(Entity objetivo) {
        boolean golpeo = super.tryAttack(objetivo);
        if (golpeo && objetivo instanceof LivingEntity vivo) {
            vivo.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 2));
        }
        return golpeo;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_STEP;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_IRON_GOLEM_DEATH;
    }
}
