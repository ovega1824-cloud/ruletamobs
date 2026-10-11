package com.mobsruleta.client;

import com.mobsruleta.OverlayPayload;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/** Filtro de color sobre la pantalla (azul al congelarse) y contador de pulsaciones de espacio. */
public final class Filtro {

    private static int argb;
    private static int ticks;
    private static int presiones;

    private Filtro() {
    }

    public static void recibir(OverlayPayload p) {
        argb = p.argb();
        ticks = p.ticks();
        presiones = p.presses();
    }

    public static int presiones() {
        return presiones;
    }

    public static void gastarPresion() {
        if (presiones > 0) {
            presiones--;
        }
    }

    public static void tick() {
        if (ticks > 0) {
            ticks--;
        }
    }

    private static boolean activo() {
        return argb != 0 && (ticks != 0 || presiones > 0);
    }

    public static void dibujar(DrawContext contexto) {
        if (!activo()) {
            return;
        }
        MinecraftClient cliente = MinecraftClient.getInstance();
        int w = contexto.getScaledWindowWidth();
        int h = contexto.getScaledWindowHeight();
        contexto.fill(0, 0, w, h, argb);
        if (presiones > 0) {
            contexto.drawCenteredTextWithShadow(cliente.textRenderer, Text.literal("¡Estás congelado!"), w / 2, h / 2 - 24, 0xFFFFFFFF);
            contexto.drawCenteredTextWithShadow(cliente.textRenderer,
                    Text.literal("Presiona ESPACIO: faltan " + presiones), w / 2, h / 2 - 10, 0xFFE0F4FF);
        }
    }
}
