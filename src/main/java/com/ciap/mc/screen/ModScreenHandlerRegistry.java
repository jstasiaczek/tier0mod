package com.ciap.mc.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;

import static com.ciap.mc.Tier0Mod.getIdentifier;

public class ModScreenHandlerRegistry {

    public static ScreenHandlerType<ChamberScreenHandler> CHAMBER_SCREEN_HANDLER;
    public static ScreenHandlerType<PlacerScreenHandler> PLACER_SCREEN_HANDLER;
    public static ScreenHandlerType<BreakerScreenHandler> BREAKER_SCREEN_HANDLER;
    public static ScreenHandlerType<OilPressScreenHandler> OIL_PRESS_SCREEN_HANDLER;
    public static ScreenHandlerType<PressScreenHandler> PRESS_SCREEN_HANDLER;
    public static ScreenHandlerType<HarvesterScreenHandler> HARVESTER_SCREEN_HANDLER;

    public static void setup() {
        CHAMBER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("chamber"), ChamberScreenHandler::new);
        PLACER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("placer"), PlacerScreenHandler::new);
        BREAKER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("breaker"), BreakerScreenHandler::new);
        OIL_PRESS_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("oil_press"), OilPressScreenHandler::new);
        PRESS_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("press"), PressScreenHandler::new);
        HARVESTER_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(getIdentifier("harvester"), HarvesterScreenHandler::new);
    }
}
