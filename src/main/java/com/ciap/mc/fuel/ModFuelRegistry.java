package com.ciap.mc.fuel;

import com.ciap.mc.block.ModBlockRegistry;
import com.ciap.mc.item.ModItemRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;

public class ModFuelRegistry {
    public final static int COAL_COKE_BURN_TIME = 1800;

    public static void setup() {
        FuelRegistry.INSTANCE.add(ModItemRegistry.COAL_COKE, COAL_COKE_BURN_TIME);
        FuelRegistry.INSTANCE.add(ModItemRegistry.FUEL_BRICK, COAL_COKE_BURN_TIME * 4);
        FuelRegistry.INSTANCE.add(ModBlockRegistry.COAL_COKE_BLOCK, COAL_COKE_BURN_TIME * 10);
    }
}
