package growthcraft.nether.common.item;

import growthcraft.core.utils.BlockCheck;
import growthcraft.core.utils.ItemUtils;
import growthcraft.nether.GrowthCraftNether;
import growthcraft.nether.common.block.BlockNetherSquashStem;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemNetherSquashSeeds extends Item implements IPlantable
{
	public ItemNetherSquashSeeds()
	{
		super();
		this.setUnlocalizedName("grcnether.netherSquashSeeds");
		this.setCreativeTab(GrowthCraftNether.tab);
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z)
	{
		return EnumPlantType.Nether;
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z)
	{
		return GrowthCraftNether.blocks.netherSquashStem.getBlock();
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z)
	{
		return 0;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int dir, float par8, float par9, float par10)
	{
		if (dir != 0) return false;

		if (player.canPlayerEdit(x, y, z, dir, stack) && player.canPlayerEdit(x, y - 1, z, dir, stack))
		{
			final BlockNetherSquashStem plant = (BlockNetherSquashStem)getPlant(world, x, y, z);
			if (BlockCheck.canSustainPlant(world, x, y, z, ForgeDirection.DOWN, plant))
			{
				world.setBlock(x, y - 1, z, plant);
				ItemUtils.consumeItem(stack);
				return true;
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg)
	{
		this.itemIcon = reg.registerIcon("grcnether:soulsquash_seeds");
	}
}