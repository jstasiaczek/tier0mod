package com.ciap.mc;

import com.ciap.mc.block.ModBlockRegistry;
import com.ciap.mc.block.entity.ModBlockEntityRegistry;
import com.ciap.mc.fuel.ModFuelRegistry;
import com.ciap.mc.item.ModItemRegistry;
import com.ciap.mc.recipe.ModRecipeRegister;
import com.ciap.mc.screen.ModScreenHandlerRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tier0Mod implements ModInitializer {
	public static final String MOD_ID = "tier0";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	@Override
	public void onInitialize() {
		ModItemRegistry.setup();
		ModBlockRegistry.setup();
		ModFuelRegistry.setup();
		ModBlockEntityRegistry.setup();
		ModRecipeRegister.setup();
		ModScreenHandlerRegistry.setup();
	}

	public static Identifier getIdentifier(String path) {
		return new Identifier(MOD_ID, path);
	}
}
