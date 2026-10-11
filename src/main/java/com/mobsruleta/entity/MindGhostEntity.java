package com.mobsruleta.entity;

import com.mobsruleta.Efectos;
import com.mobsruleta.Spawner;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Fantasma de la mente: aparece cerca de jugadores que llevan más de 4 días sin dormir. Atraviesa paredes y,
 * al tocar al jugador, le hace daño y lo deja sin ver ni moverse 7 segundos.
 */
public class MindGhostEntity extends MobBase {

    public static final int SEGUNDOS_EFECTO = 7;

    private int enfriamiento;

    public MindGhostEntity(EntityType<? extends MindGhostEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 24.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        // No camina: se mueve volando en tick(). Solo busca jugadores (aunque estén detrás de paredes).
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }

    @Override
    public void tick() {
        this.noClip = true;
        this.setNoGravity(true);

        if (!this.getWorld().isClient) {
            LivingEntity objetivo = this.getTarget();
            if (objetivo != null && objetivo.isAlive()) {
                Vec3d hacia = objetivo.getPos().add(0.0, objetivo.getHeight() * 0.6, 0.0).subtract(this.getPos());
                if (hacia.lengthSquared() > 0.01) {
                    this.setVelocity(hacia.normalize().multiply(0.09));
                }
                this.getLookControl().lookAt(objetivo, 30.0f, 30.0f);

                if (enfriamiento <= 0 && this.squaredDistanceTo(objetivo) < 2.0 && objetivo instanceof ServerPlayerEntity jugador
                        && !jugador.isSpectator()) {
                    jugador.damage(this.getDamageSources().mobAttack(this), 4.0f);
                    int ticks = SEGUNDOS_EFECTO * 20;
                    jugador.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, ticks, 0));
                    Efectos.inmovilizar(jugador, ticks);
                    enfriamiento = 60;
                }
            } else {
                this.setVelocity(this.getVelocity().multiply(0.9));
            }
            if (enfriamiento > 0) {
                enfriamiento--;
            }
            // Si ya no hay nadie cerca con más de 4 días sin dormir, se desvanece
            if (this.age % 200 == 0) {
                PlayerEntity cerca = this.getWorld().getClosestPlayer(this, 48.0);
                if (cerca == null || Spawner.sinDormir(cerca) < Spawner.TICKS_SIN_DORMIR) {
                    this.discard();
                    return;
                }
            }
        }
        super.tick();
    }

    @Override
    public boolean isInsideWall() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GHAST_DEATH;
    }
}
