package com.mobsruleta;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MobsRuletaMod implements ModInitializer {

    public static final String MOD_ID = "mobsruleta";

    public static Item RULETA;

    /** Solo los admins (operadores con nivel de permiso 2 o más) pueden usar la ruleta. */
    public static boolean esAdmin(ServerPlayerEntity player) {
        return player.hasPermissionLevel(2);
    }

    @Override
    public void onInitialize() {
        ModEntities.registrar();

        RULETA = Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "ruleta_naranja"),
                new RuletaNaranjaItem(new Item.Settings().maxCount(1)));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> entries.add(RULETA));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS)
                .register(entries -> ModEntities.EGGS.values().forEach(entries::add));

        PayloadTypeRegistry.playS2C().register(StatePayload.ID, StatePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(OverlayPayload.ID, OverlayPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LockPayload.ID, LockPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TogglePayload.ID, TogglePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(PressPayload.ID, PressPayload.CODEC);

        // El menú pide activar o desactivar la aparición de un mob. El servidor revisa que sea admin.
        ServerPlayNetworking.registerGlobalReceiver(TogglePayload.ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!esAdmin(player) || Mobs.obtener(payload.id()) == null) {
                return;
            }
            MinecraftServer server = context.server();
            MobState state = MobState.get(server);
            state.set(payload.id(), payload.activar());
            // Se aplica en silencio: solo se actualiza el estado del menú
            ServerPlayNetworking.send(player, new StatePayload(state.mask(), false));
        });

        // Pulsaciones de espacio para descongelarse
        ServerPlayNetworking.registerGlobalReceiver(PressPayload.ID,
                (payload, context) -> Efectos.pulsar(context.player()));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Efectos.tick(server);
            Spawner.tick(server);
            OjoLogic.tick(server);
        });

        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) ->
                Spawner.alRomper(world, player, pos, state));

        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
                Efectos.limpiar(handler.player.getUuid()));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            Efectos.limpiar(oldPlayer.getUuid());
            Efectos.restablecer(newPlayer);
        });
    }
}
