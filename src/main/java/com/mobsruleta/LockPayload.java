package com.mobsruleta;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Servidor -> cliente: la cámara debe seguir a esta entidad (el ojo de la demencia). entityId = -1 la suelta. */
public record LockPayload(int entityId) implements CustomPayload {

    public static final Id<LockPayload> ID = new Id<>(Identifier.of(MobsRuletaMod.MOD_ID, "camara"));

    public static final PacketCodec<RegistryByteBuf, LockPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, LockPayload::entityId,
            LockPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
