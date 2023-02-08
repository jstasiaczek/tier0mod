package com.ciap.mc.item;

import com.ciap.mc.Tier0Mod;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup {
    public static final ItemGroup tier0_GROUP = FabricItemGroupBuilder.build(Tier0Mod.getIdentifier("group"),
            () -> new ItemStack(ModItemRegistry.COAL_COKE));
}
