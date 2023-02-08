package com.ciap.mc.block;

import com.ciap.mc.item.ModItemGroup;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.PillarBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.property.Properties;
import net.minecraft.util.registry.Registry;

import java.util.function.ToIntFunction;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class ModBlockRegistry {
    private static Block registerBlock(String name, Block block, ItemGroup group) {
        registerBlockItem(name, block, group);
        return Registry.register(Registry.BLOCK, getIdentifier(name), block);
    }

    private static Item registerBlockItem(String name, Block block, ItemGroup group) {
        return Registry.register(Registry.ITEM, getIdentifier(name),
                new BlockItem(block, new FabricItemSettings().group(group)));
    }

    public static final Block COAL_COKE_BLOCK = registerBlock(
            "coal_coke_block",
            new Block(FabricBlockSettings.of(Material.STONE).strength(0.5f)),
            ModItemGroup.tier0_GROUP);

    public static final Block DOUBLE_SLAB_BLOCK = registerBlock(
            "double_slab",
            new PillarBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f)),
            ModItemGroup.tier0_GROUP);

    public static final Block COPPER_DOUBLE_SLAB_BLOCK = registerBlock(
            "copper_double_slab",
            new PillarBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool()),
            ModItemGroup.tier0_GROUP);

    public static final Block IRON_DOUBLE_SLAB_BLOCK = registerBlock(
            "iron_double_slab",
            new PillarBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool()),
            ModItemGroup.tier0_GROUP);

    public static final Block IRON_DOUBLE_SLAB_LAMP = registerBlock(
            "iron_double_slab_lamp",
            new DoubleSlabLightBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool().luminance(createLightLevelFromLitBlockState(15))),
            ModItemGroup.tier0_GROUP);

    public static final Block IRON_DOUBLE_SLAB_LAMP_CLICKABLE = registerBlock(
            "iron_double_slab_lamp_clickable",
            new ClickableDoubleSlabLightBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool().luminance(createLightLevelFromLitBlockState(15))),
            ModItemGroup.tier0_GROUP);

    public static final Block COPPER_DOUBLE_SLAB_LAMP = registerBlock(
            "copper_double_slab_lamp",
            new DoubleSlabLightBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool().luminance(createLightLevelFromLitBlockState(15))),
            ModItemGroup.tier0_GROUP);

    public static final Block COPPER_DOUBLE_SLAB_LAMP_CLICKABLE = registerBlock(
            "copper_double_slab_lamp_clickable",
            new ClickableDoubleSlabLightBlock(FabricBlockSettings.of(Material.STONE).strength(4.5f).requiresTool().luminance(createLightLevelFromLitBlockState(15))),
            ModItemGroup.tier0_GROUP);

    public static final Block CHAMBER_BLOCK = registerBlock("chamber",
            new Chamber(FabricBlockSettings.of(Material.STONE).strength(0.5f)),
            ModItemGroup.tier0_GROUP);

    public static final Block PLACER_BLOCK = registerBlock("placer",
            new Placer(FabricBlockSettings.of(Material.STONE).strength(0.5f)),
            ModItemGroup.tier0_GROUP);

    public static final Block BREAKER_BLOCK = registerBlock("breaker",
            new Breaker(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block HARVESTER_BLOCK = registerBlock("harvester",
            new Harvester(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block OIL_PRESS = registerBlock("oil_press",
            new OilPress(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block PRESS = registerBlock("press",
            new Press(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block TOILET = registerBlock("toilet",
            new Toilet(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block CRUSHER = registerBlock("crusher",
            new Crusher(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block SAWMILL = registerBlock("sawmill",
            new Sawmill(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    public static final Block TREE_CUTTER = registerBlock("tree_cutter",
            new TreeCutter(FabricBlockSettings.of(Material.STONE).strength(0.5f).nonOpaque()),
            ModItemGroup.tier0_GROUP);

    private static ToIntFunction<BlockState> createLightLevelFromLitBlockState(int litLevel) {
        return (state) -> {
            return (Boolean)state.get(Properties.LIT) ? litLevel : 0;
        };
    }

    public static void setup() {
    }
}
