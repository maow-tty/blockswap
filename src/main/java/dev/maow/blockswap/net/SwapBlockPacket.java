package dev.maow.blockswap.net;

import dev.maow.blockswap.Blockswap;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;

public record SwapBlockPacket(BlockHitResult hit) implements FabricPacket {
    public static final PacketType<SwapBlockPacket> TYPE = PacketType.create(
            Blockswap.id("swap_block"), SwapBlockPacket::new
    );

    public SwapBlockPacket(PacketByteBuf buf) {
        this(buf.readBlockHitResult());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeBlockHitResult(this.hit);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
