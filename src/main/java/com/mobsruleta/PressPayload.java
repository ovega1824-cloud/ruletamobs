package com.mobsruleta;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Cliente -> servidor: el jugador presionó espacio mientras está congelado. */
public record PressPayload() implements CustomPayload {

    public static final Id<PressPayload> ID = new Id<>(Identifier.of(MobsRuletaMod.MOD_ID, "espacio"));

    public static final PacketCodec<RegistryByteBuf, PressPayload> CODEC = PacketCodec.unit(new PressPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
