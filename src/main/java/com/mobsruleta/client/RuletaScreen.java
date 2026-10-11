package com.mobsruleta.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mobsruleta.MobsRuletaMod;
import com.mobsruleta.Mobs;
import com.mobsruleta.TogglePayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Menú de la ruleta naranja: la rueda arriba, los números en 4 columnas y, al elegir un número, un recuadro con el
 * mob, su descripción y los botones ACTIVAR y DESACTIVAR (si puede aparecer en el mundo). Fondo transparente.
 */
public class RuletaScreen extends Screen {

    private static final Identifier RUEDA = Identifier.of(MobsRuletaMod.MOD_ID, "textures/gui/ruleta_naranja.png");

    // Medidas en "coordenadas virtuales"; todo se escala si la ventana es pequeña
    private static final int ALTO_VIRTUAL = 262;
    private static final int RUEDA_TAM = 80;
    private static final int CASILLA = 20;
    private static final int SEPARACION = 4;
    private static final int COLUMNAS = 4;
    private static final int CASILLAS_Y = 92;
    private static final int PANEL_W = 250;
    private static final int PANEL_H = 90;
    private static final int PANEL_Y = 168;

    private int mask;
    private int seleccion = 0;
    private float escala = 1f;

    public RuletaScreen(int mask) {
        super(Text.literal("Ruleta"));
        this.mask = mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

    private boolean activo(int id) {
        return (mask & (1 << (id - 1))) != 0;
    }

    @Override
    protected void init() {
        escala = Math.min(1f, (this.height - 6) / (float) ALTO_VIRTUAL);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    /** Fondo transparente: no se dibuja nada. */
    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private float centroX() {
        return (this.width / escala) / 2f;
    }

    private int casillaX(int indice) {
        int total = COLUMNAS * CASILLA + (COLUMNAS - 1) * SEPARACION;
        return (int) (centroX() - total / 2f) + (indice % COLUMNAS) * (CASILLA + SEPARACION);
    }

    private int casillaY(int indice) {
        return CASILLAS_Y + (indice / COLUMNAS) * (CASILLA + SEPARACION);
    }

    private int panelX() {
        return (int) (centroX() - PANEL_W / 2f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float vx = mouseX / escala;
        float vy = mouseY / escala;

        context.getMatrices().push();
        context.getMatrices().scale(escala, escala, 1f);

        RenderSystem.enableBlend();
        context.drawTexture(RUEDA, (int) (centroX() - RUEDA_TAM / 2f), 4, RUEDA_TAM, RUEDA_TAM,
                0f, 0f, 128, 128, 128, 128);
        RenderSystem.disableBlend();

        for (int i = 0; i < Mobs.ALL.size(); i++) {
            int id = i + 1;
            int x = casillaX(i);
            int y = casillaY(i);

            context.fill(x, y, x + CASILLA, y + CASILLA, 0xFF000000);
            int color = activo(id) ? 0xFF55FF55 : 0xFFFFFFFF;
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal(String.valueOf(id)),
                    x + CASILLA / 2, y + 6, color);

            boolean encima = vx >= x && vx < x + CASILLA && vy >= y && vy < y + CASILLA;
            if (id == seleccion) {
                context.drawBorder(x - 1, y - 1, CASILLA + 2, CASILLA + 2, 0xFFFFFF00);
            } else if (encima) {
                context.drawBorder(x - 1, y - 1, CASILLA + 2, CASILLA + 2, 0xFFFFFFFF);
            }
        }

        if (seleccion > 0) {
            dibujarPanel(context);
        }
        context.getMatrices().pop();
    }

    private void dibujarPanel(DrawContext context) {
        int x = panelX();
        int y = PANEL_Y;
        Mobs.Def mob = Mobs.obtener(seleccion);
        if (mob == null) {
            return;
        }

        context.fill(x, y, x + PANEL_W, y + PANEL_H, 0xFF000000);
        context.fill(x + 2, y + 2, x + PANEL_W - 2, y + PANEL_H - 2, 0xFFFFFFFF);

        // Número arriba a la derecha
        context.getMatrices().push();
        context.getMatrices().translate(x + PANEL_W - 24, y + 6, 0);
        context.getMatrices().scale(2f, 2f, 1f);
        context.drawText(this.textRenderer, String.valueOf(seleccion), 0, 0, 0xFF000000, false);
        context.getMatrices().pop();

        // Huevo de aparición del mob (el doble de grande)
        Item huevo = Registries.ITEM.get(Identifier.of(MobsRuletaMod.MOD_ID, mob.id() + "_spawn_egg"));
        context.getMatrices().push();
        context.getMatrices().translate(x + 10, y + 10, 0);
        context.getMatrices().scale(2f, 2f, 1f);
        context.drawItem(new ItemStack(huevo), 0, 0);
        context.getMatrices().pop();

        // Nombre y qué hace
        context.drawText(this.textRenderer, "mob nuevo: " + mob.nombre(), x + 50, y + 8, 0xFF000000, false);
        List<OrderedText> lineas = this.textRenderer.wrapLines(Text.literal(mob.descripcion()), PANEL_W - 50 - 30);
        for (int i = 0; i < lineas.size() && i < 5; i++) {
            context.drawText(this.textRenderer, lineas.get(i), x + 50, y + 20 + i * 10, 0xFF000000, false);
        }

        // Botones ACTIVAR (verde) y DESACTIVAR (rojo)
        int[] a = botonActivar();
        int[] d = botonDesactivar();
        context.fill(a[0], a[1], a[0] + a[2], a[1] + a[3], 0xFF00FF00);
        context.fill(d[0], d[1], d[0] + d[2], d[1] + d[3], 0xFFFF0000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("ACTIVAR"), a[0] + a[2] / 2, a[1] + 3, 0xFF000000);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("DESACTIVAR"), d[0] + d[2] / 2, d[1] + 3, 0xFF000000);

        // Un borde negro marca en qué estado está ahora
        if (activo(seleccion)) {
            context.drawBorder(a[0], a[1], a[2], a[3], 0xFF000000);
        } else {
            context.drawBorder(d[0], d[1], d[2], d[3], 0xFF000000);
        }
    }

    /** x, y, ancho, alto */
    private int[] botonActivar() {
        return new int[]{panelX() + 8, PANEL_Y + PANEL_H - 18, 70, 12};
    }

    private int[] botonDesactivar() {
        return new int[]{panelX() + PANEL_W - 8 - 90, PANEL_Y + PANEL_H - 18, 90, 12};
    }

    private static boolean dentro(double px, double py, int[] r) {
        return px >= r[0] && px < r[0] + r[2] && py >= r[1] && py < r[1] + r[3];
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        double vx = mouseX / escala;
        double vy = mouseY / escala;

        if (seleccion > 0) {
            if (dentro(vx, vy, botonActivar())) {
                enviar(true);
                return true;
            }
            if (dentro(vx, vy, botonDesactivar())) {
                enviar(false);
                return true;
            }
        }

        for (int i = 0; i < Mobs.ALL.size(); i++) {
            int x = casillaX(i);
            int y = casillaY(i);
            if (vx >= x && vx < x + CASILLA && vy >= y && vy < y + CASILLA) {
                seleccion = i + 1;
                sonido();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void enviar(boolean activar) {
        ClientPlayNetworking.send(new TogglePayload(seleccion, activar));
        if (activar) {
            mask |= (1 << (seleccion - 1));
        } else {
            mask &= ~(1 << (seleccion - 1));
        }
        sonido();
    }

    private void sonido() {
        if (this.client != null) {
            this.client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.client != null && this.client.options.inventoryKey.matchesKey(keyCode, scanCode)) {
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
