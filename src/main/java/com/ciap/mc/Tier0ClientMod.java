package com.ciap.mc;

import com.ciap.mc.block.ModBlockRegistry;
import com.ciap.mc.block.entity.ModBlockEntityRegistry;
import com.ciap.mc.renderer.*;
import com.ciap.mc.screen.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;

public class Tier0ClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.BREAKER, BreakerBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.OIL_PRESS, OilPressBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.PRESS, PressBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.CRUSHER, CrusherBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.SAWMILL, SawmillBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.HARVESTER, HarvesterBlockRenderer::new);
        BlockEntityRendererRegistry.register(ModBlockEntityRegistry.TREE_CUTTER, TreeCutterBlockRenderer::new);
//        ModBlockEntityRegistry.setup();
        ScreenRegistry.register(ModScreenHandlerRegistry.CHAMBER_SCREEN_HANDLER, ChamberScreen::new);
        ScreenRegistry.register(ModScreenHandlerRegistry.PLACER_SCREEN_HANDLER, PlacerScreen::new);
        ScreenRegistry.register(ModScreenHandlerRegistry.BREAKER_SCREEN_HANDLER, BreakerScreen::new);
        ScreenRegistry.register(ModScreenHandlerRegistry.OIL_PRESS_SCREEN_HANDLER, OilPressScreen::new);
        ScreenRegistry.register(ModScreenHandlerRegistry.PRESS_SCREEN_HANDLER, PressScreen::new);
        ScreenRegistry.register(ModScreenHandlerRegistry.HARVESTER_SCREEN_HANDLER, HarvesterScreen::new);

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlockRegistry.CRUSHER, RenderLayer.getTranslucent());
    }
}
