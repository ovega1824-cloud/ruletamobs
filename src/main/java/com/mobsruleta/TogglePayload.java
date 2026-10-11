package com.mobsruleta;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: activar o desactivar la aparición del mob número id. */
public record TogglePayload(int id, boolean activar) implements CustomPayload {

    public static final Id<TogglePayload> ID = new Id<>(Identifier.of(MobsRuletaMod.MOD_ID, "cambiar"));

    public static final PacketCodec<RegistryByteBuf, TogglePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.VAR_INT, TogglePayload::id,
            PacketCodecs.BOOL, TogglePayload::activar,
            TogglePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
