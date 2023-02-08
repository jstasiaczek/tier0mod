package com.ciap.mc.block.entity;


import com.ciap.mc.block.ModBlockRegistry;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.registry.Registry;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class ModBlockEntityRegistry {
    public static BlockEntityType<ChamberBlockEntity> CHAMBER;
    public static BlockEntityType<PlacerBlockEntity> PLACER;
    public static BlockEntityType<BreakerBlockEntity> BREAKER;
    public static BlockEntityType<OilPressBlockEntity> OIL_PRESS;
    public static BlockEntityType<PressBlockEntity> PRESS;
    public static BlockEntityType<CrusherBlockEntity> CRUSHER;
    public static BlockEntityType<SawmillBlockEntity> SAWMILL;
    public static BlockEntityType<HarvesterBlockEntity> HARVESTER;
    public static BlockEntityType<TreeCutterBlockEntity> TREE_CUTTER;

    public static void setup() {
          CHAMBER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                  getIdentifier("chamber"),
                  FabricBlockEntityTypeBuilder.create(ChamberBlockEntity::new, ModBlockRegistry.CHAMBER_BLOCK).build(null));

          PLACER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                 getIdentifier("placer"),
                 FabricBlockEntityTypeBuilder.create(PlacerBlockEntity::new, ModBlockRegistry.PLACER_BLOCK).build(null));

          BREAKER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                 getIdentifier("breaker"),
                 FabricBlockEntityTypeBuilder.create(BreakerBlockEntity::new, ModBlockRegistry.BREAKER_BLOCK).build(null));

          OIL_PRESS = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                 getIdentifier("oil_press"),
                 FabricBlockEntityTypeBuilder.create(OilPressBlockEntity::new, ModBlockRegistry.OIL_PRESS).build(null));

          PRESS = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                getIdentifier("press"),
                FabricBlockEntityTypeBuilder.create(PressBlockEntity::new, ModBlockRegistry.PRESS).build(null));

          CRUSHER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                getIdentifier("crusher"),
                FabricBlockEntityTypeBuilder.create(CrusherBlockEntity::new, ModBlockRegistry.CRUSHER).build(null));

          SAWMILL = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                getIdentifier("sawmill"),
                FabricBlockEntityTypeBuilder.create(SawmillBlockEntity::new, ModBlockRegistry.SAWMILL).build(null));

          HARVESTER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                getIdentifier("harvester"),
                FabricBlockEntityTypeBuilder.create(HarvesterBlockEntity::new, ModBlockRegistry.HARVESTER_BLOCK).build(null));

        TREE_CUTTER = Registry.register(Registry.BLOCK_ENTITY_TYPE,
                getIdentifier("tree_cutter"),
                FabricBlockEntityTypeBuilder.create(TreeCutterBlockEntity::new, ModBlockRegistry.TREE_CUTTER).build(null));
    }
}
