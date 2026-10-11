package com.mobsruleta;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Servidor -> cliente: filtro de color sobre la pantalla (azul al congelarse, gris al petrificarse).
 * ticks = cuántos ticks dura (-1 = hasta que presiones espacio las veces que faltan); presses = pulsaciones que faltan.
 * Con ticks = 0 y presses = 0 se quita el filtro.
 */
public record OverlayPayload(int argb, int ticks, int presses) implements CustomPayload {

    public static final Id<OverlayPayload> ID = new Id<>(Identifier.of(MobsRuletaMod.MOD_ID, "filtro"));

    public static final PacketCodec<RegistryByteBuf, OverlayPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.INTEGER, OverlayPayload::argb,
            PacketCodecs.INTEGER, OverlayPayload::ticks,
            PacketCodecs.INTEGER, OverlayPayload::presses,
            OverlayPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
