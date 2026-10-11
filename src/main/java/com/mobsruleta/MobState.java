package com.mobsruleta;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.World;

/** Guarda en el mundo qué mobs tienen la aparición activada (un bit por mob; por defecto todos activados). */
public class MobState extends PersistentState {

    public static final Type<MobState> TYPE = new Type<>(MobState::new, MobState::fromNbt, null);

    private int mask = (1 << Mobs.ALL.size()) - 1;

    public MobState() {
    }

    public static MobState fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        MobState state = new MobState();
        if (nbt.contains("mask")) {
            state.mask = nbt.getInt("mask");
        }
        return state;
    }

    public static MobState get(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, "mobsruleta");
    }

    /** true si el mob con ese id (por ejemplo "snow_creeper") puede aparecer. */
    public static boolean activo(World world, String id) {
        MinecraftServer server = world.getServer();
        if (server == null) {
            return true;
        }
        int n = Mobs.numero(id);
        return n == 0 || get(server).isEnabled(n);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putInt("mask", mask);
        return nbt;
    }

    public int mask() {
        return mask;
    }

    public boolean isEnabled(int numero) {
        return (mask & (1 << (numero - 1))) != 0;
    }

    public void set(int numero, boolean enabled) {
        if (enabled) {
            mask |= (1 << (numero - 1));
        } else {
            mask &= ~(1 << (numero - 1));
        }
        markDirty();
    }
}
