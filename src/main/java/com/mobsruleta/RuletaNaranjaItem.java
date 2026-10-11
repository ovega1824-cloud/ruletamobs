package com.mobsruleta;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** La ruleta naranja. Con clic derecho abre el menú, pero solo para admins. */
public class RuletaNaranjaItem extends Item {

    public RuletaNaranjaItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (!world.isClient && user instanceof ServerPlayerEntity player) {
            if (MobsRuletaMod.esAdmin(player)) {
                int mask = MobState.get(player.getServer()).mask();
                ServerPlayNetworking.send(player, new StatePayload(mask, true));
            } else {
                player.sendMessage(Text.literal("Solo los admins pueden usar la ruleta."), true);
            }
        }

        return TypedActionResult.success(stack, world.isClient);
    }
}
