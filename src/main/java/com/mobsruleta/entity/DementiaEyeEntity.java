package com.mobsruleta.entity;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Ojo de la demencia: placa que queda pegada a una pared o al techo y no se mueve. Quien lo ve queda con la cámara fija
 * en él (ver OjoLogic). La cara indica hacia dónde mira el ojo (lejos de la superficie donde está pegado).
 */
public class DementiaEyeEntity extends MobBase {

    private static final TrackedData<Integer> CARA = DataTracker.registerData(DementiaEyeEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public DementiaEyeEntity(EntityType<? extends DementiaEyeEntity> type, World world) {
        super(type, world);
        this.setNoGravity(true);
        this.setAiDisabled(true);
    }

    public static DefaultAttributeContainer.Builder atributos() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 12.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 8.0);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CARA, Direction.SOUTH.getId());
    }

    /** Hacia dónde mira el ojo. */
    public Direction cara() {
        return Direction.byId(this.dataTracker.get(CARA));
    }

    /** Lo pega a la superficie sólida que está en esa dirección (por ejemplo UP = techo). */
    public void pegarA(Direction superficie) {
        this.dataTracker.set(CARA, superficie.getOpposite().getId());
        BlockPos bloque = BlockPos.ofFloored(this.getX(), this.getY() + 0.3, this.getZ());
        Vec3d centro = Vec3d.ofCenter(bloque);
        this.setPosition(centro.x + superficie.getOffsetX() * 0.45,
                centro.y - 0.3 + superficie.getOffsetY() * 0.45,
                centro.z + superficie.getOffsetZ() * 0.45);
    }

    @Override
    protected void initGoals() {
        // No hace nada: solo está pegado en su lugar
    }

    @Override
    public void tick() {
        if (!this.getWorld().isClient && (this.age == 2 || this.age % 40 == 0)) {
            comprobarSoporte();
        }
        super.tick();
    }

    /** Si la superficie desapareció busca otra cercana; si no hay ninguna, se desvanece. */
    private void comprobarSoporte() {
        BlockPos bloque = BlockPos.ofFloored(this.getX(), this.getY() + 0.3, this.getZ());
        Direction soporte = cara().getOpposite();
        if (solido(bloque.offset(soporte))) {
            return;
        }
        for (Direction d : Direction.values()) {
            if (solido(bloque.offset(d))) {
                pegarA(d);
                return;
            }
        }
        this.discard();
    }

    private boolean solido(BlockPos pos) {
        BlockState estado = this.getWorld().getBlockState(pos);
        return estado.isSolidBlock(this.getWorld(), pos);
    }

    @Override
    public boolean isInsideWall() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void takeKnockback(double strength, double x, double z) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ENDERMAN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ENDERMAN_DEATH;
    }
}
