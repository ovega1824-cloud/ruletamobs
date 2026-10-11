package com.mobsruleta.client;

import com.mobsruleta.LockPayload;
import com.mobsruleta.MobsRuletaMod;
import com.mobsruleta.Mobs;
import com.mobsruleta.ModEntities;
import com.mobsruleta.OverlayPayload;
import com.mobsruleta.PressPayload;
import com.mobsruleta.StatePayload;
import com.mobsruleta.entity.CreeperBase;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.Identifier;

public class MobsRuletaClient implements ClientModInitializer {

    public static EntityModelLayer capa(String id) {
        return new EntityModelLayer(Identifier.of(MobsRuletaMod.MOD_ID, id), "main");
    }

    @Override
    public void onInitializeClient() {
        for (Mobs.Def def : Mobs.ALL) {
            String id = def.id();
            EntityModelLayerRegistry.registerModelLayer(capa(id), () -> ModelDefs.datos(id));
            registrar(ModEntities.tipo(id), id);
        }

        ClientPlayNetworking.registerGlobalReceiver(StatePayload.ID, (payload, context) -> {
            MinecraftClient cliente = context.client();
            if (payload.abrir()) {
                cliente.setScreen(new RuletaScreen(payload.mask()));
            } else if (cliente.currentScreen instanceof RuletaScreen pantalla) {
                pantalla.setMask(payload.mask());
            }
        });
        ClientPlayNetworking.registerGlobalReceiver(OverlayPayload.ID, (payload, context) -> Filtro.recibir(payload));
        ClientPlayNetworking.registerGlobalReceiver(LockPayload.ID, (payload, context) -> Camara.fijar(payload.entityId()));

        ClientTickEvents.START_CLIENT_TICK.register(cliente -> Camara.aplicar(cliente));
        ClientTickEvents.END_CLIENT_TICK.register(cliente -> {
            Filtro.tick();
            // Cada pulsación de espacio mientras estás congelado se cuenta
            while (cliente.options.jumpKey.wasPressed()) {
                if (Filtro.presiones() > 0) {
                    ClientPlayNetworking.send(new PressPayload());
                    Filtro.gastarPresion();
                }
            }
        });
        HudRenderCallback.EVENT.register((contexto, tickCounter) -> {
            Camara.aplicar(MinecraftClient.getInstance());
            Filtro.dibujar(contexto);
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends HostileEntity> void registrar(EntityType<T> tipo, String id) {
        EntityRendererRegistry.register(tipo, contexto -> {
            if (id.equals("dementia_eye")) {
                return (EntityRenderer<T>) (EntityRenderer) new EyeRenderer(contexto, id);
            }
            if (id.endsWith("creeper")) {
                return (EntityRenderer<T>) (EntityRenderer) new CreeperRenderer<CreeperBase>(contexto, id);
            }
            float sombra = id.equals("air_dragon") ? 0.6f : (id.equals("mud_golem") ? 0.8f : 0.4f);
            return new MobRenderer<T>(contexto, id, sombra);
        });
    }
}
