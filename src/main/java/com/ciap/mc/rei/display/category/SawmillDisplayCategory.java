package com.ciap.mc.rei.display.category;

import com.ciap.mc.block.ModBlockRegistry;
import com.ciap.mc.block.entity.SawmillBlockEntity;
import com.ciap.mc.rei.Tier0ReiClientPlugin;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class SawmillDisplayCategory extends PressLikeDisplayCategory {
    public SawmillDisplayCategory() {
    }

    @Override
    public CategoryIdentifier getCategoryIdentifier() {
        return Tier0ReiClientPlugin.SAWMILL_DISPLAY_CATEGORY_IDENTIFIER;
    }

    @Override
    public Text getTitle() {
        return Text.translatable("text.sawmill.title");
    }

    @Override
    public Renderer getIcon() {
        return EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(ModBlockRegistry.SAWMILL));
    }

    @Override
    protected int getOilUsage() {
        return SawmillBlockEntity.OIL_SUBTRACT;
    }
}
