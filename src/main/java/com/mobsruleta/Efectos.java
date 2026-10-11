package com.mobsruleta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/** Efectos que los mobs aplican a los jugadores: inmovilizar, congelar (espacio x200) y lanzar por los aires. */
public final class Efectos {

    public static final int COLOR_HIELO = 0x803090FF;

    private static final Identifier MOD_VELOCIDAD = Identifier.of(MobsRuletaMod.MOD_ID, "inmovil_velocidad");
    private static final Identifier MOD_SALTO = Identifier.of(MobsRuletaMod.MOD_ID, "inmovil_salto");

    /** Ticks que le quedan a cada jugador inmovilizado. */
    private static final Map<UUID, Integer> INMOVIL = new HashMap<>();
    /** Pulsaciones de espacio que le faltan a cada jugador congelado. */
    private static final Map<UUID, Integer> CONGELADO = new HashMap<>();

    private Efectos() {
    }

    /** No se puede mover ni saltar durante estos ticks. */
    public static void inmovilizar(ServerPlayerEntity jugador, int ticks) {
        INMOVIL.merge(jugador.getUuid(), ticks, Math::max);
        aplicar(jugador, true);
    }

    /** Congela al jugador: filtro azul y hay que presionar espacio esta cantidad de veces. */
    public static void congelar(ServerPlayerEntity jugador, int pulsaciones) {
        if (jugador.isSpectator()) {
            return;
        }
        CONGELADO.put(jugador.getUuid(), pulsaciones);
        aplicar(jugador, true);
        ServerPlayNetworking.send(jugador, new OverlayPayload(COLOR_HIELO, -1, pulsaciones));
    }

    public static void pulsar(ServerPlayerEntity jugador) {
        Integer faltan = CONGELADO.get(jugador.getUuid());
        if (faltan == null) {
            return;
        }
        faltan--;
        if (faltan <= 0) {
            CONGELADO.remove(jugador.getUuid());
            ServerPlayNetworking.send(jugador, new OverlayPayload(0, 0, 0));
        } else {
            CONGELADO.put(jugador.getUuid(), faltan);
            ServerPlayNetworking.send(jugador, new OverlayPayload(COLOR_HIELO, -1, faltan));
        }
    }

    /** Lo manda hacia arriba con esta velocidad vertical (2.3 sube unos 25 bloques) y luego cae. */
    public static void lanzar(ServerPlayerEntity jugador, double velocidadVertical) {
        jugador.setVelocity(0.0, velocidadVertical, 0.0);
        jugador.velocityModified = true;
        jugador.fallDistance = 0.0f;
        jugador.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(jugador));
    }

    public static void limpiar(UUID id) {
        INMOVIL.remove(id);
        CONGELADO.remove(id);
    }

    /** Quita filtros y cámara fija (al reaparecer). */
    public static void restablecer(ServerPlayerEntity jugador) {
        ServerPlayNetworking.send(jugador, new OverlayPayload(0, 0, 0));
        ServerPlayNetworking.send(jugador, new LockPayload(-1));
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, Integer>> it = INMOVIL.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Integer> e = it.next();
            if (e.getValue() <= 1) {
                it.remove();
            } else {
                e.setValue(e.getValue() - 1);
            }
        }
        for (ServerPlayerEntity jugador : server.getPlayerManager().getPlayerList()) {
            boolean inmovil = INMOVIL.containsKey(jugador.getUuid()) || CONGELADO.containsKey(jugador.getUuid());
            aplicar(jugador, inmovil);
        }
    }

    private static void aplicar(ServerPlayerEntity jugador, boolean activo) {
        modificador(jugador, EntityAttributes.GENERIC_MOVEMENT_SPEED, MOD_VELOCIDAD, activo);
        modificador(jugador, EntityAttributes.GENERIC_JUMP_STRENGTH, MOD_SALTO, activo);
    }

    private static void modificador(ServerPlayerEntity jugador, RegistryEntry<EntityAttribute> atributo, Identifier id, boolean activo) {
        EntityAttributeInstance instancia = jugador.getAttributeInstance(atributo);
        if (instancia == null) {
            return;
        }
        if (activo) {
            if (!instancia.hasModifier(id)) {
                instancia.addTemporaryModifier(new EntityAttributeModifier(id, -1.0,
                        EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
        } else if (instancia.hasModifier(id)) {
            instancia.removeModifier(id);
        }
    }
}
