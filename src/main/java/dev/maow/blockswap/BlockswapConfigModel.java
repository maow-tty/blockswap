package dev.maow.blockswap;

import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;

import java.util.List;

@Modmenu(modId = Blockswap.MODID)
@Config(name = "blockswap-config", wrapperName = "BlockswapConfig")
public class BlockswapConfigModel {
    public List<String> blacklistedIdentifiers = List.of("minecraft:tall_grass", "minecraft:pointed_dripstone");
    public List<String> blacklistedTags = List.of("minecraft:beds", "minecraft:doors");
}