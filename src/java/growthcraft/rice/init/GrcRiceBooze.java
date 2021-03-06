package growthcraft.rice.init;

import growthcraft.api.cellar.booze.Booze;
import growthcraft.api.cellar.booze.BoozeTag;
import growthcraft.api.cellar.common.Residue;
import growthcraft.api.core.effect.EffectAddPotionEffect;
import growthcraft.api.core.effect.EffectWeightedRandomList;
import growthcraft.api.core.effect.SimplePotionEffectFactory;
import growthcraft.api.core.util.TickUtils;
import growthcraft.cellar.common.definition.BlockBoozeDefinition;
import growthcraft.cellar.common.definition.ItemBucketBoozeDefinition;
import growthcraft.cellar.common.item.ItemBoozeBottle;
import growthcraft.cellar.common.item.ItemBoozeBucketDEPRECATED;
import growthcraft.cellar.GrowthCraftCellar;
import growthcraft.cellar.util.BoozeRegistryHelper;
import growthcraft.cellar.util.YeastType;
import growthcraft.core.common.definition.ItemDefinition;
import growthcraft.core.common.GrcModuleBase;
import growthcraft.core.GrowthCraftCore;
import growthcraft.rice.GrowthCraftRice;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;

public class GrcRiceBooze extends GrcModuleBase
{
	public Booze[] riceSakeBooze;
	public BlockBoozeDefinition[] riceSakeFluids;
	public ItemDefinition riceSake;
	public ItemDefinition riceSakeBucket_deprecated;
	public ItemBucketBoozeDefinition[] riceSakeBuckets;

	@Override
	public void preInit()
	{
		riceSakeBooze = new Booze[7];
		riceSakeFluids = new BlockBoozeDefinition[riceSakeBooze.length];
		riceSakeBuckets = new ItemBucketBoozeDefinition[riceSakeBooze.length];
		BoozeRegistryHelper.initializeBooze(riceSakeBooze, riceSakeFluids, riceSakeBuckets, "grc.riceSake", GrowthCraftRice.getConfig().riceSakeColor);
		riceSakeBooze[4].setColor(GrowthCraftRice.getConfig().riceSakeDivineColor);
		riceSakeFluids[4].getBlock().refreshColor();
		riceSake = new ItemDefinition(new ItemBoozeBottle(5, -0.6F, riceSakeBooze));
		riceSakeBucket_deprecated = new ItemDefinition(new ItemBoozeBucketDEPRECATED(riceSakeBooze).setColor(GrowthCraftRice.getConfig().riceSakeColor));
	}

	private void registerRecipes()
	{
		final float defaultTipsy = 0.65f;
		final int fermentTime = GrowthCraftCellar.getConfig().fermentTime;
		final FluidStack[] fs = new FluidStack[riceSakeBooze.length];
		for (int i = 0; i < fs.length; ++i)
		{
			fs[i] = new FluidStack(riceSakeBooze[i], 1);
		}

		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[0])
			.tags(BoozeTag.YOUNG)
			.brewsFrom(
				new FluidStack(FluidRegistry.WATER, 40),
				GrowthCraftRice.rice.asStack(),
				TickUtils.minutes(1),
				Residue.newDefault(0.2F));

		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[1])
			.tags(BoozeTag.FERMENTED)
			.fermentsFrom(fs[0], YeastType.BREWERS.asStack(), fermentTime)
			.fermentsFrom(fs[0], new ItemStack(Items.nether_wart), (int)(fermentTime * 0.66))
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.addPotionEntry(Potion.jump, TickUtils.minutes(3), 0);

		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[2])
			.tags(BoozeTag.FERMENTED, BoozeTag.POTENT)
			.fermentsFrom(fs[1], new ItemStack(Items.glowstone_dust), fermentTime)
			.fermentsFrom(fs[3], new ItemStack(Items.glowstone_dust), fermentTime)
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.addPotionEntry(Potion.jump, TickUtils.minutes(3), 0);

		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[3])
			.tags(BoozeTag.FERMENTED, BoozeTag.EXTENDED)
			.fermentsFrom(fs[1], new ItemStack(Items.redstone), fermentTime)
			.fermentsFrom(fs[2], new ItemStack(Items.redstone), fermentTime)
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.addPotionEntry(Potion.jump, TickUtils.minutes(3), 0);

		// Ethereal Yeast - Divine Sake
		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[4])
			.tags(BoozeTag.FERMENTED, BoozeTag.HYPER_EXTENDED)
			.fermentsFrom(fs[2], YeastType.ETHEREAL.asStack(), fermentTime)
			.fermentsFrom(fs[3], YeastType.ETHEREAL.asStack(), fermentTime)
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.addPotionEntry(Potion.jump, TickUtils.minutes(3), 0)
				.addPotionEntry(Potion.moveSpeed, TickUtils.minutes(3), 0);

		// Origin Yeast
		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[5])
			.tags(BoozeTag.FERMENTED, BoozeTag.INTOXICATED)
			.fermentsFrom(fs[2], YeastType.ORIGIN.asStack(), fermentTime)
			.fermentsFrom(fs[3], YeastType.ORIGIN.asStack(), fermentTime)
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.addEffect(new EffectWeightedRandomList()
					.add(8, new EffectAddPotionEffect(new SimplePotionEffectFactory(Potion.jump.id, TickUtils.minutes(3), 2)))
					.add(2, new EffectAddPotionEffect(new SimplePotionEffectFactory(Potion.confusion.id, TickUtils.minutes(3), 2))));

		// Poisoned Sake - created from netherrash,
		// the booze looses all its benefits and effectively becomes poisoned
		GrowthCraftCellar.boozeBuilderFactory.create(riceSakeBooze[6])
			.tags(BoozeTag.FERMENTED, BoozeTag.POISONED)
			//.fermentsFrom(fs[1], YeastType.NETHERRASH.asStack(), fermentTime)
			.getEffect()
				.setTipsy(defaultTipsy, TickUtils.seconds(45))
				.createPotionEntry(Potion.poison, TickUtils.seconds(90), 0).toggleDescription(!GrowthCraftCore.getConfig().hidePoisonedBooze);
	}

	@Override
	public void register()
	{
		GameRegistry.registerItem(riceSake.getItem(), "grc.riceSake");
		GameRegistry.registerItem(riceSakeBucket_deprecated.getItem(), "grc.riceSake_bucket");

		BoozeRegistryHelper.registerBooze(riceSakeBooze, riceSakeFluids, riceSakeBuckets, riceSake, "grc.riceSake", riceSakeBucket_deprecated);
		registerRecipes();
	}
}
