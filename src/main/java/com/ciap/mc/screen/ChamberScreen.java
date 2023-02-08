package com.ciap.mc.screen;

import com.ciap.mc.Tier0Mod;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ChamberScreen extends HandledScreen<ChamberScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Tier0Mod.MOD_ID, "textures/gui/chamber_gui.png");

    public ChamberScreen(ChamberScreenHandler handler, PlayerInventory inventory, Text title) {
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

        if(handler.isCrafting()) {
            drawTexture(matrices, x + 74, y + 35, 176, 27, handler.getScaledProgress(), 36);
        }

        if(handler.getChamberType() > 0) {
            Integer spritePos = handler.getChamberType() == 1 ? 192 : 176;
            drawTexture(matrices, x + 79, y + 16, spritePos, 0, 15, 15);
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}