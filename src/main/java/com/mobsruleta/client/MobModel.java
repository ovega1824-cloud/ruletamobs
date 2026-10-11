package com.mobsruleta.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;

/** Modelo genérico: dibuja las partes de mobs.json y las anima según su rol (cabeza, patas, alas...). */
public class MobModel<T extends LivingEntity> extends EntityModel<T> {

    private record Parte(ModelPart parte, String rol, float fase) {
    }

    private final ModelPart raiz;
    private final List<Parte> partes = new ArrayList<>();

    public MobModel(ModelPart raiz, Map<String, String> roles) {
        super(RenderLayer::getEntityCutoutNoCull);
        this.raiz = raiz;
        for (Map.Entry<String, String> e : roles.entrySet()) {
            partes.add(new Parte(raiz.getChild(e.getKey()), e.getValue(), (e.getKey().hashCode() & 7)));
        }
    }

    @Override
    public void setAngles(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch) {
        float paso = limbAngle * 0.6662f;
        for (Parte p : partes) {
            ModelPart m = p.parte();
            switch (p.rol()) {
                case "head" -> {
                    m.yaw = headYaw * 0.017453292f;
                    m.pitch = headPitch * 0.017453292f;
                }
                case "leg0", "arm1" -> m.pitch = MathHelper.cos(paso) * 1.4f * limbDistance;
                case "leg1", "arm0" -> m.pitch = MathHelper.cos(paso + (float) Math.PI) * 1.4f * limbDistance;
                case "wing0" -> m.roll = MathHelper.cos(animationProgress * 0.5f) * 0.7f;
                case "wing1" -> m.roll = -MathHelper.cos(animationProgress * 0.5f) * 0.7f;
                case "sway" -> {
                    m.roll = MathHelper.sin(animationProgress * 0.12f + p.fase()) * 0.12f;
                    m.pitch = MathHelper.cos(animationProgress * 0.1f + p.fase()) * 0.1f;
                }
                default -> {
                }
            }
        }
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        raiz.render(matrices, vertices, light, overlay, color);
    }
}
