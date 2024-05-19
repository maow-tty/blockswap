package dev.maow.blockswap.client;

import dev.maow.blockswap.Blockswap;
import dev.maow.blockswap.net.SwapBlockPacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.option.StickyKeyBinding;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public final class BlockswapClient implements ClientModInitializer {
    public static final Identifier HOTBAR_OFFHAND_LEFT_SWAP_TEXTURE = Blockswap.id("hud/hotbar_offhand_left_swap");
    public static final Identifier HOTBAR_OFFHAND_RIGHT_SWAP_TEXTURE = Blockswap.id("hud/hotbar_offhand_right_swap");

    public static final KeyBinding TOGGLE_SWAP_KEYBINDING;
    static {
        TOGGLE_SWAP_KEYBINDING = KeyBindingHelper.registerKeyBinding(new StickyKeyBinding(
                "key.blockswap.toggle_swap",
                GLFW.GLFW_KEY_GRAVE_ACCENT,
                KeyBinding.GAMEPLAY_CATEGORY,
                () -> true
        ));
    }

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_SWAP_KEYBINDING.wasPressed()) {
                final ClientWorld world = client.world;
                final ClientPlayerEntity player = client.player;
                if (world != null && player != null) {
                    world.playSound(player,
                            player.getX(), player.getY(), player.getZ(),
                            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, SoundCategory.MASTER
                    );
                }
            }
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (world.isClient()
                    && !player.isSpectator()
                    && TOGGLE_SWAP_KEYBINDING.isPressed()
            ) {
                if (!player.hasStackEquipped(EquipmentSlot.OFFHAND)) return ActionResult.PASS;
                ClientPlayNetworking.send(new SwapBlockPacket(hit));
                return ActionResult.FAIL; // prevent automatic processing/packet
            }
            return ActionResult.PASS;
        });
    }
}
