package com.setycz.chickens.liquidEgg;

import com.setycz.chickens.LiquidEggRegistry;
import com.setycz.chickens.LiquidEggRegistryItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import java.util.List;

/**
 * Created by setyc on 14.02.2016.
 */
public class ItemLiquidEgg extends ItemEgg {
    public ItemLiquidEgg() {
        setHasSubtypes(true);
    }

    @Override
    public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
        for (LiquidEggRegistryItem liquid : LiquidEggRegistry.getAll()) {
            subItems.add(new ItemStack(itemIn, 1, liquid.getId()));
        }
    }

    /*
    @Override
    public int getColorFromItemStack(ItemStack stack, int renderPass) {
        return LiquidEggRegistry.findById(stack.getMetadata()).getEggColor();
    }
    */

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Block liquid = LiquidEggRegistry.findById(stack.getMetadata()).getLiquid();
        return I18n.translateToLocal(getUnlocalizedName() + "." + liquid.getUnlocalizedName().substring(5) + ".name");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand) {
        RayTraceResult raytraceresult = this.getMovingObjectPositionFromPlayer(worldIn, playerIn, false);
        if (raytraceresult == null) {
            return new ActionResult(EnumActionResult.PASS, itemStackIn);
        }
        else if (raytraceresult.typeOfHit != RayTraceResult.Type.BLOCK) {
            return new ActionResult(EnumActionResult.PASS, itemStackIn);
        }
        else {
            BlockPos blockpos = raytraceresult.getBlockPos();
            if (!worldIn.isBlockModifiable(playerIn, blockpos))
            {
                return new ActionResult(EnumActionResult.FAIL, itemStackIn);
            }
            else {
                boolean flag1 = worldIn.getBlockState(blockpos).getBlock().isReplaceable(worldIn, blockpos);
                BlockPos blockpos1 = flag1 && raytraceresult.sideHit == EnumFacing.UP ? blockpos : blockpos.offset(raytraceresult.sideHit);

                Block liquid = LiquidEggRegistry.findById(itemStackIn.getMetadata()).getLiquid();
                if (!playerIn.canPlayerEdit(blockpos1, raytraceresult.sideHit, itemStackIn))
                {
                    return new ActionResult(EnumActionResult.FAIL, itemStackIn);
                }
                else if (this.tryPlaceContainedLiquid(playerIn, worldIn, blockpos1, liquid))
                {
                    playerIn.addStat(StatList.func_188057_b(this));
                    return !playerIn.capabilities.isCreativeMode ? new ActionResult(EnumActionResult.SUCCESS, new ItemStack(itemStackIn.getItem(), itemStackIn.stackSize - 1, itemStackIn.getMetadata())) : new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
                }
                else
                {
                    return new ActionResult(EnumActionResult.FAIL, itemStackIn);
                }
            }
        }
    }

    public boolean tryPlaceContainedLiquid(EntityPlayer playerIn, World worldIn, BlockPos pos, Block liquid) {
        Material material = worldIn.getBlockState(pos).getMaterial();
        boolean flag = !material.isSolid();

        if (!worldIn.isAirBlock(pos) && !flag) {
            return false;
        } else {
            if (worldIn.provider.doesWaterVaporize() && liquid == Blocks.flowing_water) {
                int i = pos.getX();
                int j = pos.getY();
                int k = pos.getZ();
                worldIn.playSound(playerIn, pos, SoundEvents.block_fire_extinguish, SoundCategory.BLOCKS, 0.5F, 2.6F + (worldIn.rand.nextFloat() - worldIn.rand.nextFloat()) * 0.8F);

                for (int l = 0; l < 8; ++l) {
                    worldIn.spawnParticle(EnumParticleTypes.SMOKE_LARGE, (double) i + Math.random(), (double) j + Math.random(), (double) k + Math.random(), 0.0D, 0.0D, 0.0D, new int[0]);
                }
            } else {
                if (!worldIn.isRemote && flag && !material.isLiquid()) {
                    worldIn.destroyBlock(pos, true);
                }

                worldIn.setBlockState(pos, liquid.getDefaultState(), 3);
            }

            return true;
        }
    }
}
