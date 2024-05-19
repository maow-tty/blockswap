package dev.maow.blockswap;

import com.google.common.math.DoubleMath;
import dev.maow.blockswap.BlockswapConfig;
import dev.maow.blockswap.net.SwapBlockPacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public final class Blockswap implements ModInitializer {
    public static final String MODID = "blockswap";
    public static final BlockswapConfig CONFIG = BlockswapConfig.createAndLoad();

    @Override
    public void onInitialize() {
        ServerPlayNetworking.registerGlobalReceiver(
                SwapBlockPacket.TYPE,
                (packet, player, sender) -> doSwapBlock(packet.hit(), player)
        );
    }

    private static void doSwapBlock(BlockHitResult hit, ServerPlayerEntity player) {
        final ServerWorld world = player.getServerWorld();

        final BlockPos destPos = hit.getBlockPos();
        final BlockState destBlock = world.getBlockState(destPos);

        final ItemStack mainStack = player.getMainHandStack();

        final boolean creative = player.isCreative();
        if (!creative && !isSuitable(mainStack, destBlock, world, destPos)) return;

        final ItemStack offStack = player.getOffHandStack();

        if (offStack.getItem() instanceof BlockItem offItem) {
            final Block srcBlock = offItem.getBlock();
            final Identifier offId = Registries.BLOCK.getId(srcBlock);

            if (CONFIG.blacklistedIdentifiers().contains(offId.toString())
                    || inBlacklistedTags(srcBlock)) {
                cannotSwap(player, srcBlock);
                return;
            }

            // Attempt to swap the blocks and return if unsuccessful.
            if (!trySwapBlock(world, player, destPos, hit, offStack, offItem).isAccepted()) return;
            mainStack.damage(1, player, entity -> entity.sendToolBreakStatus(Hand.OFF_HAND));

            player.swingHand(Hand.OFF_HAND, true);

            // Removes source items and adds dropped destination items.
            if (!creative) {
                swapItems(world, player, mainStack, destBlock, destPos);
            }
        }
    }

    private static ActionResult trySwapBlock(ServerWorld world, ServerPlayerEntity player,
                                             BlockPos destPos, BlockHitResult hit,
                                             ItemStack offStack, BlockItem offItem) {
        world.setBlockState(destPos, Blocks.AIR.getDefaultState());
        return offItem.place(new ItemPlacementContext(world, player, Hand.OFF_HAND, offStack, hit));
    }

    private static void swapItems(ServerWorld world, ServerPlayerEntity player,
                           ItemStack tool,
                           BlockState destBlock, BlockPos destPos) {
        final LootContextParameterSet.Builder builder = new LootContextParameterSet.Builder(world)
                .add(LootContextParameters.ORIGIN, Vec3d.ofCenter(destPos))
                .add(LootContextParameters.TOOL, tool);

        for (ItemStack droppedStack : destBlock.getDroppedStacks(builder)) {
            // If off-hand is full, add the dropped stack anywhere in the inventory.
            // Otherwise, put it directly in the off-hand.
            if (player.hasStackEquipped(EquipmentSlot.OFFHAND)) {
                player.giveItemStack(droppedStack);
            } else {
                player.getInventory().insertStack(PlayerInventory.OFF_HAND_SLOT, droppedStack);
            }
        }
    }

    private static void cannotSwap(ServerPlayerEntity player, Block block) {
        player.sendMessage(Text.translatable("text.blockswap.cannot_swap", block.getName()).formatted(Formatting.RED), true);
    }

    private static boolean isSuitable(ItemStack stack, BlockState block, ServerWorld world, BlockPos destPos) {
        if (!stack.isEmpty() && stack.getItem() instanceof ToolItem item) {
            // If tool is known to be suitable (including edge-cases like shears).
            if (item.isSuitableFor(stack, block)) return true;
        }

        final float hardness = block.getHardness(world, destPos);
        if (DoubleMath.fuzzyEquals(hardness, -1, 0.00001D)) {
            return false;
        }

        // Otherwise, test if the block doesn't require any tool.
        return !block.isToolRequired();
    }

    private static boolean inBlacklistedTags(Block block) {
        for (String str : CONFIG.blacklistedTags()) {
            final Identifier id = new Identifier(str);
            final TagKey<Block> tag = TagKey.of(Registries.BLOCK.getKey(), id);
            if (Registries.BLOCK.getEntry(block).isIn(tag)) {
                return true;
            }
        }
        return false;
    }

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
