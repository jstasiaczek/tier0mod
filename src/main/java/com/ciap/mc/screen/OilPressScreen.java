package com.ciap.mc.screen;

import com.ciap.mc.Tier0Mod;
import com.ciap.mc.block.entity.OilPressBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class OilPressScreen extends HandledScreen<OilPressScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Tier0Mod.MOD_ID, "textures/gui/oil_press_gui.png");

    public OilPressScreen(OilPressScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawTexture(matrices, x, y, 0, 0, backgroundWidth, backgroundHeight);

        if(handler.hasFuel()) {
            drawTexture(matrices, x + 30, y + 16 + 14 - handler.getScaledFuelProgress(), 176,
                    14 - handler.getScaledFuelProgress(), 14, handler.getScaledFuelProgress());
        }

        if(handler.isCrafting()) {
            drawTexture(matrices, x + 74, y + 35, 176, 27, handler.getScaledProgress(), 12);
        }

        drawTexture(matrices, x + 104, y + 15 + 61 - handler.getOilProgress(), 176 + (handler.isExtractingUranium() ? 17 : 0),
                43 + 61 - handler.getOilProgress(), 16, handler.getOilProgress());

        drawStringWithShadow(matrices, MinecraftClient.getInstance().textRenderer, handler.getOilText(), x+128, y+35, 0xFFFFFF);
        drawStringWithShadow(matrices, MinecraftClient.getInstance().textRenderer, "/"+ OilPressBlockEntity.MAX_OIL, x+123, y+45, 0xFFFFFF);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}