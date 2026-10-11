package com.mobsruleta.client;

import com.mobsruleta.entity.DementiaEyeEntity;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;

/** Renderer del ojo de la demencia: lo orienta hacia donde mira (pared, techo o suelo). */
public class EyeRenderer extends MobRenderer<DementiaEyeEntity> {

    public EyeRenderer(EntityRendererFactory.Context contexto, String id) {
        super(contexto, id, 0.0f);
    }

    @Override
    public void render(DementiaEyeEntity ojo, float yaw, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light) {
        matrices.push();
        // El centro del ojo está 0.3 bloques arriba de la posición de la entidad
        matrices.translate(0.0, 0.3, 0.0);
        Direction cara = ojo.cara();
        if (cara == Direction.UP) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-90.0f));
        } else if (cara == Direction.DOWN) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
        } else {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-cara.asRotation()));
        }
        matrices.translate(0.0, -0.3, 0.0);
        super.render(ojo, 0.0f, tickDelta, matrices, vertexConsumers, light);
        matrices.pop();
    }
}
