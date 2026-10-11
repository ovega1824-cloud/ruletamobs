package com.mobsruleta.client;

import com.mobsruleta.entity.CreeperBase;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

/** Igual que MobRenderer pero el creeper se hincha mientras se enciende la mecha. */
public class CreeperRenderer<T extends CreeperBase> extends MobRenderer<T> {

    public CreeperRenderer(EntityRendererFactory.Context contexto, String id) {
        super(contexto, id, 0.5f);
    }

    @Override
    protected void scale(T entity, MatrixStack matrices, float amount) {
        float f = entity.progresoMecha();
        matrices.scale(1.0f + f * 0.2f, 1.0f + f * 0.1f, 1.0f + f * 0.2f);
    }
}
