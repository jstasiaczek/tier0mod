package com.ciap.mc.screen;

import com.ciap.mc.Tier0Mod;
import com.ciap.mc.block.entity.HarvesterBlockEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class HarvesterScreen extends HandledScreen<HarvesterScreenHandler> {
    private static final Identifier TEXTURE =
            new Identifier(Tier0Mod.MOD_ID, "textures/gui/harvester_gui.png");

    public HarvesterScreen(HarvesterScreenHandler handler, PlayerInventory inventory, Text title) {
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

        drawTexture(matrices, x + 153, y + 17 + 61 - handler.getCokeProgress(), 176,
                61 - handler.getCokeProgress(), 16, handler.getCokeProgress());

        drawStringWithShadow(matrices, MinecraftClient.getInstance().textRenderer, handler.getCokeText(), x+133, y+35, 0xFFFFFF);
        drawStringWithShadow(matrices, MinecraftClient.getInstance().textRenderer, "/"+ HarvesterBlockEntity.CHUNK_MAX_COUNT, x+127, y+45, 0xFFFFFF);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }
}