package growthcraft.nether.common.block;

import java.util.Random;

import growthcraft.core.block.ICropBlock;
import growthcraft.core.block.ICropDataProvider;
import growthcraft.core.integration.AppleCore;
import growthcraft.core.utils.BlockFlags;
import growthcraft.core.utils.RenderType;
import growthcraft.nether.GrowthCraftNether;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockNetherPepper extends BlockBush implements ICropDataProvider, ICropBlock
{
	public static class PepperStages
	{
		public static final int SEEDLING = 0;
		public static final int YOUNG = 1;
		public static final int FULL = 2;
		public static final int FRUIT = 3;
		public static final int COUNT = 4;

		private PepperStages() {}
	}

	protected IIcon[] icons;
	private int minPepperPicked = GrowthCraftNether.getConfig().minPepperPicked;
	private int maxPepperPicked = GrowthCraftNether.getConfig().maxPepperPicked;

	public BlockNetherPepper()
	{
		super(Material.plants);
		setTickRandomly(true);
		setBlockTextureName("grcnether:pepper");
		setBlockName("grcnether.netherPepper");
	}

	private void incrementGrowth(World world, int x, int y, int z, int meta)
	{
		world.setBlockMetadataWithNotify(x, y, z, meta + 1, BlockFlags.UPDATE_CLIENT);
		AppleCore.announceGrowthTick(this, world, x, y, z, meta);
	}

	@Override
	public boolean isFullyGrown(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z) >= PepperStages.FRUIT;
	}

	@Override
	public boolean canGrow(World world, int x, int y, int z)
	{
		return world.getBlockMetadata(x, y, z) < PepperStages.FRUIT;
	}

	@Override
	public boolean onUseBonemeal(World world, int x, int y, int z)
	{
		if (canGrow(world, x, y, z))
		{
			if (!world.isRemote)
			{
				incrementGrowth(world, x, y, z, world.getBlockMetadata(x, y, z));
			}
			return true;
		}
		return false;
	}

	@Override
	public float getGrowthProgress(IBlockAccess world, int x, int y, int z, int meta)
	{
		return (float)meta / (float)PepperStages.FRUIT;
	}

	protected boolean func_149854_a(Block block)
	{
		return Blocks.soul_sand == block;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random)
	{
		final Event.Result result = AppleCore.validateGrowthTick(this, world, x, y, z, random);
		if (Event.Result.DENY == result) return;

		if (canGrow(world, x, y, z))
		{
			if (Event.Result.ALLOW == result || random.nextInt(10) == 0)
			{
				incrementGrowth(world, x, y, z, world.getBlockMetadata(x, y, z));
			}
		}
		super.updateTick(world, x, y, z, random);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World world, int x, int y, int z)
	{
		return GrowthCraftNether.items.netherPepper.getItem();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float par7, float par8, float par9)
	{
		if (isFullyGrown(world, x, y, z))
		{
			if (!world.isRemote)
			{
				world.setBlockMetadataWithNotify(x, y, z, PepperStages.FULL, BlockFlags.UPDATE_CLIENT);
				final int count = minPepperPicked + world.rand.nextInt(maxPepperPicked - minPepperPicked);
				dropBlockAsItem(world, x, y, z, GrowthCraftNether.items.netherPepper.asStack(count));
			}
			return true;
		}
		return false;
	}

	@Override
	public int getRenderType()
	{
		return RenderType.CROPS;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg)
	{
		this.icons = new IIcon[PepperStages.COUNT];

		for (int stage = 0; stage < icons.length; ++stage)
		{
			icons[stage] = reg.registerIcon(this.getTextureName() + "_stage_" + stage);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta)
	{
		if (meta >= PepperStages.SEEDLING && meta <= PepperStages.FRUIT)
		{
			return icons[meta];
		}
		return icons[PepperStages.FRUIT];
	}
}
