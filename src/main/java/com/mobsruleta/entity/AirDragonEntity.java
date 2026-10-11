package com.mobsruleta.entity;

import java.util.UUID;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Dragón de aire: patrulla el cielo. Cuando ve a un jugador baja en picada, lo agarra y lo sube más de 24 bloques;
 * luego lo suelta (y el jugador cae).
 */
public class AirDragonEntity extends MobBase {

    /** Cuántos bloques sube al jugador antes de soltarlo. */
    public static final double ALTURA_SUBIDA = 26.0;

    private UUID presa;
    private double inicioY;
    private int enfriamiento;
    private int cambioRumbo;
    private double alturaBase = Double.NaN;
    private Vec3d rumbo = Vec3d.ZERO;

    public AirDragonEntity(EntityType<? extends AirDragonEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 30.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 5.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 48.0);
    }

    @Override
    protected void initGoals() {
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public void tick() {
        this.setNoGravity(true);
        if (!this.getWorld().isClient) {
            if (Double.isNaN(alturaBase)) {
                alturaBase = this.getY();
            }
            if (presa != null) {
                llevarPresa();
            } else {
                LivingEntity objetivo = this.getTarget();
                if (objetivo != null && objetivo.isAlive() && enfriamiento <= 0 && this.canSee(objetivo)) {
                    Vec3d hacia = objetivo.getPos().add(0.0, objetivo.getHeight() * 0.5, 0.0).subtract(this.getPos());
                    this.setVelocity(hacia.normalize().multiply(0.5));
                    mirar(this.getVelocity());
                    if (this.squaredDistanceTo(objetivo) < 6.0 && objetivo instanceof ServerPlayerEntity jugador
                            && !jugador.isSpectator()) {
                        presa = jugador.getUuid();
                        inicioY = this.getY();
                    }
                } else {
                    patrullar();
                }
            }
            if (enfriamiento > 0) {
                enfriamiento--;
            }
        }
        super.tick();
    }

    private void patrullar() {
        if (cambioRumbo-- <= 0) {
            cambioRumbo = 40 + this.random.nextInt(40);
            double ang = this.random.nextDouble() * Math.PI * 2.0;
            rumbo = new Vec3d(Math.cos(ang) * 0.15, 0.0, Math.sin(ang) * 0.15);
        }
        double dy = MathHelper.clamp((alturaBase - this.getY()) * 0.02, -0.05, 0.05);
        this.setVelocity(rumbo.x, dy, rumbo.z);
        mirar(rumbo);
    }

    private void llevarPresa() {
        PlayerEntity p = this.getWorld().getPlayerByUuid(presa);
        if (!(p instanceof ServerPlayerEntity jugador) || !jugador.isAlive() || jugador.isSpectator()
                || this.getY() - inicioY > ALTURA_SUBIDA || this.age % 1200 == 0) {
            soltar();
            return;
        }
        this.setVelocity(0.0, 0.4, 0.0);
        jugador.fallDistance = 0.0f;
        jugador.networkHandler.requestTeleport(this.getX(), this.getY() - 2.3, this.getZ(), jugador.getYaw(), jugador.getPitch());
    }

    private void soltar() {
        presa = null;
        enfriamiento = 400;
        this.setTarget(null);
    }

    private void mirar(Vec3d v) {
        if (v.horizontalLengthSquared() > 1.0E-4) {
            float yaw = (float) (MathHelper.atan2(-v.x, v.z) * 57.29578);
            this.setYaw(yaw);
            this.bodyYaw = yaw;
            this.headYaw = yaw;
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;
    }
}
