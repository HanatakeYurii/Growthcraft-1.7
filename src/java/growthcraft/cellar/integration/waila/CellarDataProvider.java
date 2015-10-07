package growthcraft.cellar.integration.waila;

import java.util.List;

import growthcraft.cellar.block.BlockFruitPresser;
import growthcraft.cellar.tileentity.TileEntityBrewKettle;
import growthcraft.cellar.tileentity.TileEntityFruitPress;
import growthcraft.cellar.tileentity.TileEntityFermentBarrel;
import growthcraft.cellar.utils.TagFormatterBrewKettle;
import growthcraft.cellar.utils.TagFormatterFruitPress;
import growthcraft.cellar.utils.TagFormatterFermentBarrel;
import growthcraft.core.utils.ConstID;
import growthcraft.core.utils.TagFormatterFluidHandler;

import cpw.mods.fml.common.Optional;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class CellarDataProvider implements IWailaDataProvider
{
	@Override
	@Optional.Method(modid = "Waila")
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return accessor.getStack();
	}

	@Override
	@Optional.Method(modid = "Waila")
	public List<String> getWailaHead(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return tooltip;
	}

	@Override
	@Optional.Method(modid = "Waila")
	public List<String> getWailaBody(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		final Block block = accessor.getBlock();
		final TileEntity te = accessor.getTileEntity();
		if (block instanceof BlockFruitPresser)
		{
			tooltip.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("grc.cellar.fruitPresser.state_prefix") + " " +
				EnumChatFormatting.WHITE + StatCollector.translateToLocal("grc.cellar.fruitPresser.state." +
					((BlockFruitPresser)block).getPressStateName(accessor.getMetadata())));
		}
		final NBTTagCompound tag = accessor.getNBTData();
		if (config.getConfig("FermentBarrelExtras"))
		{
			if (te instanceof TileEntityFermentBarrel)
			{
				tooltip = TagFormatterFermentBarrel.INSTANCE.format(tooltip, tag);
			}
		}
		if (config.getConfig("BrewKettleExtras"))
		{
			if (te instanceof TileEntityBrewKettle)
			{
				tooltip = TagFormatterBrewKettle.INSTANCE.format(tooltip, tag);
			}
		}
		if (config.getConfig("FruitPressExtras"))
		{
			if (te instanceof TileEntityFruitPress)
			{
				tooltip = TagFormatterFruitPress.INSTANCE.format(tooltip, tag);
			}
		}
		if (config.getConfig("DisplayFluidContent"))
		{
			if (te instanceof IFluidHandler)
			{
				tooltip = TagFormatterFluidHandler.INSTANCE.format(tooltip, tag);
			}
		}
		return tooltip;
	}

	@Override
	@Optional.Method(modid = "Waila")
	public List<String> getWailaTail(ItemStack itemStack, List<String> tooltip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return tooltip;
	}

	private void getIFluidHandlerData(IFluidHandler fluidHandler, NBTTagCompound tag)
	{
		final NBTTagList tankTagList = new NBTTagList();
		int tankId = 0;
		for (FluidTankInfo tankInfo : fluidHandler.getTankInfo(ForgeDirection.UNKNOWN))
		{
			final NBTTagCompound tankTag = new NBTTagCompound();
			tankTag.setInteger("tank_id", tankId);
			tankTag.setInteger("capacity", tankInfo.capacity);
			if (tankInfo.fluid != null)
			{
				tankTag.setInteger("fluid_id", tankInfo.fluid.getFluidID());
				tankTag.setInteger("amount", tankInfo.fluid.amount);
			}
			else
			{
				// no fluid
				tankTag.setInteger("fluid_id", ConstID.NO_FLUID);
				tankTag.setInteger("amount", 0);
			}
			tankTagList.appendTag(tankTag);
			++tankId;
		}
		tag.setTag("tanks", tankTagList);
		tag.setInteger("tank_count", tankId);
	}

	private NBTTagCompound getItemData(ItemStack itemStack, NBTTagCompound tag)
	{
		if (itemStack != null)
		{
			final Item item = itemStack.getItem();
			tag.setInteger("id", (item != null) ? Item.getIdFromItem(item) : ConstID.NO_ITEM);
			tag.setInteger("damage", itemStack.getItemDamage());
			tag.setInteger("size", itemStack.stackSize);
		}
		else
		{
			tag.setInteger("id", ConstID.NO_ITEM);
			tag.setInteger("damage", 0);
			tag.setInteger("size", 0);
		}
		return tag;
	}

	private void getBrewKettleData(TileEntityBrewKettle brewKettle, NBTTagCompound tag)
	{
		tag.setBoolean("can_brew", brewKettle.canBrew());
		tag.setTag("item_brew", getItemData(brewKettle.getStackInSlot(0), new NBTTagCompound()));
		tag.setTag("item_residue", getItemData(brewKettle.getStackInSlot(1), new NBTTagCompound()));
	}

	private void getFruitPressData(TileEntityFruitPress fruitPress, NBTTagCompound tag)
	{
		tag.setTag("item_press", getItemData(fruitPress.getStackInSlot(0), new NBTTagCompound()));
		tag.setTag("item_residue", getItemData(fruitPress.getStackInSlot(1), new NBTTagCompound()));
	}

	private void getFermentBarrelData(TileEntityFermentBarrel fermentBarrel, NBTTagCompound tag)
	{
		tag.setTag("item_modifier", getItemData(fermentBarrel.getStackInSlot(0), new NBTTagCompound()));
		tag.setInteger("time", fermentBarrel.getTime());
		tag.setInteger("time_max", fermentBarrel.getTimeMax());
		final FluidStack fluidStack = fermentBarrel.getFluidStack();
		if (fluidStack != null)
		{
			tag.setInteger("booze_id", fluidStack.getFluidID());
		}
		else
		{
			tag.setInteger("booze_id", ConstID.NO_FLUID);
		}
	}

	@Override
	@Optional.Method(modid = "Waila")
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, int x, int y, int z)
	{
		if (te instanceof IFluidHandler) getIFluidHandlerData((IFluidHandler)te, tag);
		if (te instanceof TileEntityBrewKettle) getBrewKettleData((TileEntityBrewKettle)te, tag);
		if (te instanceof TileEntityFruitPress) getFruitPressData((TileEntityFruitPress)te, tag);
		if (te instanceof TileEntityFermentBarrel) getFermentBarrelData((TileEntityFermentBarrel)te, tag);
		return tag;
	}
}