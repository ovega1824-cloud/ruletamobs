package com.mobsruleta.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

/** Mantiene la mira del jugador fija en el ojo de la demencia que está viendo. */
public final class Camara {

    private static int entidad = -1;

    private Camara() {
    }

    public static void fijar(int id) {
        entidad = id;
    }

    public static void aplicar(MinecraftClient cliente) {
        if (entidad < 0 || cliente.player == null || cliente.world == null) {
            return;
        }
        Entity ojo = cliente.world.getEntityById(entidad);
        if (ojo == null) {
            return;
        }
        double dx = ojo.getX() - cliente.player.getX();
        double dy = (ojo.getY() + 0.3) - cliente.player.getEyeY();
        double dz = ojo.getZ() - cliente.player.getZ();
        double h = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (MathHelper.atan2(dz, dx) * 57.29578) - 90.0f;
        float pitch = (float) (-(MathHelper.atan2(dy, h) * 57.29578));
        cliente.player.setYaw(yaw);
        cliente.player.setPitch(pitch);
        cliente.player.prevYaw = yaw;
        cliente.player.prevPitch = pitch;
    }
}
