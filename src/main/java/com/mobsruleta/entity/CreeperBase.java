package com.mobsruleta.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

/**
 * Creeper propio: se acerca al jugador, se enciende la mecha cuando está cerca y llama a {@link #explotar(ServerWorld)}.
 * Cada creeper nuevo decide qué pasa al explotar.
 */
public abstract class CreeperBase extends MobBase {

    private static final TrackedData<Integer> MECHA = DataTracker.registerData(CreeperBase.class, TrackedDataHandlerRegistry.INTEGER);

    protected static final int MECHA_MAX = 30;

    private int mecha;

    protected CreeperBase(EntityType<? extends HostileEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder atributosCreeper(double vida, double velocidad) {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, vida)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, velocidad)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(MECHA, 0);
    }

    /** Qué pasa al explotar. La entidad se elimina justo después. */
    protected abstract void explotar(ServerWorld mundo);

    /** 0 a 1: qué tan encendida está la mecha (para que el creeper se hinche). */
    public float progresoMecha() {
        return this.dataTracker.get(MECHA) / (float) MECHA_MAX;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(6, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new RevengeGoal(this));
    }

    /** Los creepers no pegan, solo explotan. */
    @Override
    public boolean tryAttack(Entity target) {
        return false;
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient && this.isAlive()) {
            LivingEntity objetivo = this.getTarget();
            if (objetivo != null && this.squaredDistanceTo(objetivo) < 9.0 && this.canSee(objetivo)) {
                if (mecha == 0) {
                    this.playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f);
                }
                mecha++;
                this.dataTracker.set(MECHA, mecha);
                if (mecha >= MECHA_MAX) {
                    if (this.getWorld() instanceof ServerWorld mundo) {
                        explotar(mundo);
                    }
                    this.discard();
                    return;
                }
            } else if (mecha > 0) {
                mecha--;
                this.dataTracker.set(MECHA, mecha);
            }
        }
        super.tick();
    }

    protected void sonidoExplosion(ServerWorld mundo) {
        mundo.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE, 3.0f, 0.9f);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CREEPER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CREEPER_DEATH;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }
}
