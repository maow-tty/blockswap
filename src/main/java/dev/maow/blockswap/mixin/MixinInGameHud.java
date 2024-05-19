package dev.maow.blockswap.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.maow.blockswap.client.BlockswapClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public abstract class MixinInGameHud {
    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;HOTBAR_OFFHAND_LEFT_TEXTURE:Lnet/minecraft/util/Identifier;"))
    private Identifier swapLeftOffHand(Identifier original) {
        return BlockswapClient.TOGGLE_SWAP_KEYBINDING.isPressed() ? BlockswapClient.HOTBAR_OFFHAND_LEFT_SWAP_TEXTURE : original;
    }

    @ModifyExpressionValue(method = "renderHotbar", at = @At(value = "FIELD", target = "Lnet/minecraft/client/gui/hud/InGameHud;HOTBAR_OFFHAND_RIGHT_TEXTURE:Lnet/minecraft/util/Identifier;"))
    private Identifier swapRightOffHand(Identifier original) {
        return BlockswapClient.TOGGLE_SWAP_KEYBINDING.isPressed() ? BlockswapClient.HOTBAR_OFFHAND_RIGHT_SWAP_TEXTURE : original;
    }
}