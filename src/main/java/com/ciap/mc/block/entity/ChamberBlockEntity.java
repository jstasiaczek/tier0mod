package com.ciap.mc.block.entity;

import com.ciap.mc.block.Chamber;
import com.ciap.mc.recipe.ChamberRecipe;
import com.ciap.mc.screen.ChamberScreenHandler;
import com.ciap.mc.utils.ImplementedInventory;
import com.ciap.mc.utils.TagUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class ChamberBlockEntity extends BlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    protected final PropertyDelegate propertyDelegate;
    private int progress = 0;
    private int maxProgress = 100;
    private int chamberType = 0;
    public static int SLOT_ITEM_TO_PROCESS = 0;
    public static int SLOT_RECIPE_ADDITION = 1;
    public static int SLOT_RESULT = 1;
    public static int SLOT_RESULT_ADDITIONAL = 2;
    public ChamberBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityRegistry.CHAMBER, pos, state);
        this.propertyDelegate = new PropertyDelegate() {
            public int get(int index) {
                switch (index) {
                    case 0: return ChamberBlockEntity.this.progress;
                    case 1: return ChamberBlockEntity.this.maxProgress;
                    case 2: return ChamberBlockEntity.this.chamberType;
                    default: return 0;
                }
            }
            public void set(int index, int value) {
                switch(index) {
                    case 0: ChamberBlockEntity.this.progress = value; break;
                    case 1: ChamberBlockEntity.this.maxProgress = value; break;
                    case 2: ChamberBlockEntity.this.chamberType = value; break;
                }
            }
            public int size() {
                return 3;
            }
        };

    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("text.chamber.title");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        return new ChamberScreenHandler(syncId, inv, this, this.propertyDelegate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("chamber.progress", progress);
        nbt.putInt("chamber.chamberType", chamberType);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);
        progress = nbt.getInt("chamber.progress");
        chamberType = nbt.getInt("chamber.chamberType");
    }

    private static Integer getChamberType(BlockState bs) {
        Block block = bs.getBlock();
        Integer chamberType = 0;
        if (TagUtils.containsBlock(block, BlockTags.ICE)) chamberType = 1;
        if (TagUtils.containsBlock(block, BlockTags.CAMPFIRES)) chamberType = 2;

        return chamberType;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ChamberBlockEntity entity) {
        // do some magic stuff
        BlockState bs = world.getBlockState(pos.down());
        Integer chamberType = getChamberType(bs);
        Boolean redstonePowered = world.isReceivingRedstonePower(pos);
        if (chamberType != entity.chamberType) {
            entity.chamberType = chamberType;
            world.setBlockState(pos, state.with(Chamber.TYPE, chamberType), 3);
        }

        // TODO: consider checking item type ??

        if(hasRecipe(entity) && !redstonePowered) {
            entity.progress++;
            if(entity.progress > entity.maxProgress) {
                craftItem(entity);
            }
        } else {
            entity.resetProgress();
        }
    }

    private static SimpleInventory getRecipeInventory(ChamberBlockEntity entity) {
        SimpleInventory inventory = new SimpleInventory(2);

        inventory.setStack(SLOT_ITEM_TO_PROCESS, entity.getStack(SLOT_ITEM_TO_PROCESS));

        // get block under chamber as second recipe component
        if (entity.chamberType == 1) inventory.setStack(SLOT_RECIPE_ADDITION, new ItemStack(Blocks.ICE));
        if (entity.chamberType == 2) inventory.setStack(SLOT_RECIPE_ADDITION, new ItemStack(Blocks.CAMPFIRE));

        return inventory;
    }

    private static void craftItem(ChamberBlockEntity entity) {
        World world = entity.world;
        SimpleInventory recipeInventory = getRecipeInventory(entity);

        Optional<ChamberRecipe> match = world.getRecipeManager()
                .getFirstMatch(ChamberRecipe.TypeSerializer.INSTANCE, recipeInventory, world);

        if(match.isPresent()) {
            ItemStack container = getContainer(recipeInventory, entity.getStack(SLOT_RESULT_ADDITIONAL).getCount() + 1);
            entity.removeStack(SLOT_ITEM_TO_PROCESS,1);
            entity.setStack(SLOT_RESULT, new ItemStack(match.get().getOutput().getItem(),
                    entity.getStack(SLOT_RESULT).getCount() + match.get().getOutput().getCount()));
            entity.setStack(SLOT_RESULT_ADDITIONAL, container);

            entity.resetProgress();
        }
    }

    private void resetProgress() {
        this.progress = 0;
    }

    private static ItemStack getContainer(SimpleInventory inventory) {
        return getContainer(inventory, 1);
    }
    private static ItemStack getContainer(SimpleInventory inventory, Integer count) {
        Item item = inventory.getStack(SLOT_ITEM_TO_PROCESS).getItem();
        if (item == Items.WATER_BUCKET) {
            return new ItemStack(Items.BUCKET, count);
        } else if (item == Items.GLASS_BOTTLE) {
            Potion potion = PotionUtil.getPotion(inventory.getStack(SLOT_ITEM_TO_PROCESS));
            if (Potions.WATER == potion) {
                return new ItemStack(Items.GLASS_BOTTLE, count);
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean hasRecipe(ChamberBlockEntity entity) {
        World world = entity.world;
        SimpleInventory recipeInventory = getRecipeInventory(entity);

        Optional<ChamberRecipe> match = world.getRecipeManager()
                .getFirstMatch(ChamberRecipe.TypeSerializer.INSTANCE, recipeInventory, world);

        if (!match.isPresent() || match.isEmpty()) {
            return false;
        }
        ItemStack container = getContainer(recipeInventory);

        boolean canProcess = match.isPresent() && canInsertItemIntoOutputSlot(entity, match.get().getOutput(), container);

        entity.maxProgress = match.get().getProcessTime();

        return canProcess;
    }

    private static boolean canInsertItemIntoOutputSlot(ChamberBlockEntity entity, ItemStack output, ItemStack emptyContainer) {
        return (
                (entity.getStack(SLOT_RESULT).getItem() == output.getItem()  && entity.getStack(SLOT_RESULT).getCount() < entity.getStack(SLOT_RESULT).getMaxCount())
                        || entity.getStack(SLOT_RESULT).isEmpty()
        ) && (
                (entity.getStack(SLOT_RESULT_ADDITIONAL).getItem() == emptyContainer.getItem()  && entity.getStack(SLOT_RESULT_ADDITIONAL).getCount() < entity.getStack(SLOT_RESULT_ADDITIONAL).getMaxCount())
                        || entity.getStack(SLOT_RESULT_ADDITIONAL).isEmpty()
        );
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction side) {
        return slot == 0;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction side) {
        return slot == 2;
    }
}
