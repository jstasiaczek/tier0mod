package com.ciap.mc.item;

import com.ciap.mc.utils.TagUtils;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HammerTool extends ToolItem {
    private final TagKey<Block> effectiveBlocks;
    protected final float miningSpeed;
    private final float attackDamage;
    private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

    public HammerTool(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Item.Settings settings) {
        super(material, settings);
        this.effectiveBlocks = effectiveBlocks;
        this.miningSpeed = material.getMiningSpeedMultiplier();
        this.attackDamage = attackDamage + material.getAttackDamage();
        ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Tool modifier", (double)this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
        builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Tool modifier", (double)attackSpeed, EntityAttributeModifier.Operation.ADDITION));
        this.attributeModifiers = builder.build();
    }

    @Override
    public boolean isDamageable() {
        return false;
    }

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        return false;
    }

    public float getMiningSpeedMultiplier(ItemStack stack, BlockState state) {
        return state.isIn(this.effectiveBlocks) ? this.miningSpeed : 1.0F;
    }

    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (world.isClient || !miner.isPlayer()) {
            return true;
        }
        PlayerEntity player = (PlayerEntity) miner;
        ItemStack offHand = ItemStack.EMPTY;
        if (player.getInventory().offHand.size() > 0) {
            offHand = player.getInventory().offHand.get(0);
        }

        if (!offHand.isEmpty() && offHand.getItem().equals(ModItemRegistry.GREEN_LANTERN) && offHand.getDamage() < offHand.getMaxDamage()){
            BlockPos start = pos.add(-1, -1, -1);
            BlockPos end = pos.add(1, 1, 1);
            int destroyedBlocks = 0;
            Iterable<BlockPos> toDestroy = BlockPos.iterate(start, end);
            for (BlockPos blockPos : toDestroy) {
                BlockState blockState = world.getBlockState(blockPos);
                if (TagUtils.containsBlock(blockState.getBlock(), effectiveBlocks)) {
                    destroyedBlocks++;
                    Block.dropStacks(blockState, world, blockPos);
                    world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
                }
            }
            if (destroyedBlocks == 0) {
                return true;
            }
            player.getInventory().offHand.get(0).damage(destroyedBlocks, miner, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.OFFHAND);
            });
        } else {
            stack.damage(2, miner, (e) -> {
                e.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
            });
        }

        return true;
    }

    public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? this.attributeModifiers : super.getAttributeModifiers(slot);
    }

    public float getAttackDamage() {
        return this.attackDamage;
    }

    public boolean isSuitableFor(BlockState state) {
        int i = this.getMaterial().getMiningLevel();
        if (i < 3 && state.isIn(BlockTags.NEEDS_DIAMOND_TOOL)) {
            return false;
        } else if (i < 2 && state.isIn(BlockTags.NEEDS_IRON_TOOL)) {
            return false;
        } else {
            return i < 1 && state.isIn(BlockTags.NEEDS_STONE_TOOL) ? false : state.isIn(this.effectiveBlocks);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable("item.tier0.hammer.tooltip1"));
        tooltip.add(Text.translatable("item.tier0.hammer.tooltip2"));
    }
}
