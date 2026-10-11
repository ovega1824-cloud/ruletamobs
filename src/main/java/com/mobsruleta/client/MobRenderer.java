package com.mobsruleta.client;

import com.mobsruleta.MobsRuletaMod;

import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Identifier;

/** Renderer de todos los mobs nuevos: modelo de mobs.json + textura propia. */
public class MobRenderer<T extends MobEntity> extends MobEntityRenderer<T, MobModel<T>> {

    private final Identifier textura;

    public MobRenderer(EntityRendererFactory.Context contexto, String id, float sombra) {
        super(contexto, new MobModel<>(contexto.getPart(MobsRuletaClient.capa(id)), ModelDefs.roles(id)), sombra);
        this.textura = Identifier.of(MobsRuletaMod.MOD_ID, "textures/entity/" + id + ".png");
    }

    @Override
    public Identifier getTexture(T entity) {
        return textura;
    }
}
