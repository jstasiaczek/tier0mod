package com.ciap.mc.rei.display;

import com.ciap.mc.rei.Tier0ReiClientPlugin;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.List;

public class ChamberDisplay extends BasicDisplay {

    public ChamberDisplay(List<EntryIngredient> inputs, List<EntryIngredient> outputs) {
        super(inputs, outputs);
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return Tier0ReiClientPlugin.CHAMBER_DISPLAY_CATEGORY_IDENTIFIER;
    }
}
