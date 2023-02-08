package com.ciap.mc.utils;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public class TagUtils {
    public static Boolean containsBlock(Block block, TagKey<Block> tagKey) {
        return Registry.BLOCK.getOrCreateEntry(Registry.BLOCK.getKey(block).get()).isIn(tagKey);
    };
    public static Boolean containsItem(Item item, TagKey<Item> tagKey) {
        return Registry.ITEM.getOrCreateEntry(Registry.ITEM.getKey(item).get()).isIn(tagKey);
    };
}
