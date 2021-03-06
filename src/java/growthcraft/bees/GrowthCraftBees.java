package growthcraft.bees;

import growthcraft.api.bees.BeesRegistry;
import growthcraft.api.bees.user.UserBeesConfig;
import growthcraft.api.bees.user.UserFlowersConfig;
import growthcraft.api.bees.user.UserFlowerEntry;
//import growthcraft.api.bees.user.UserHoneyConfig;
import growthcraft.api.core.log.GrcLogger;
import growthcraft.api.core.log.ILogger;
import growthcraft.api.core.module.ModuleContainer;
import growthcraft.bees.client.gui.GuiHandlerBees;
import growthcraft.bees.common.block.BlockBeeBox;
import growthcraft.bees.common.block.BlockBeeHive;
import growthcraft.bees.common.CommonProxy;
import growthcraft.bees.common.item.ItemBlockBeeBox;
import growthcraft.bees.common.tileentity.TileEntityBeeBox;
import growthcraft.bees.common.village.ComponentVillageApiarist;
import growthcraft.bees.common.village.VillageHandlerBees;
import growthcraft.bees.common.village.VillageHandlerBeesApiarist;
import growthcraft.bees.common.world.WorldGeneratorBees;
import growthcraft.bees.creativetab.CreativeTabsGrowthcraftBees;
import growthcraft.bees.init.GrcBeesBooze;
import growthcraft.bees.init.GrcBeesItems;
import growthcraft.cellar.GrowthCraftCellar;
import growthcraft.core.common.definition.BlockDefinition;
import growthcraft.core.common.definition.BlockTypeDefinition;
import growthcraft.core.GrowthCraftCore;
import growthcraft.core.util.MapGenHelper;

import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.VillagerRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.OreDictionary;

@Mod(
	modid = GrowthCraftBees.MOD_ID,
	name = GrowthCraftBees.MOD_NAME,
	version = GrowthCraftBees.MOD_VERSION,
	dependencies = "required-after:Growthcraft@@VERSION@;required-after:Growthcraft|Cellar@@VERSION@;after:Forestry"
)
public class GrowthCraftBees
{
	public static final String MOD_ID = "Growthcraft|Bees";
	public static final String MOD_NAME = "Growthcraft Bees";
	public static final String MOD_VERSION = "@VERSION@";

	@Instance(MOD_ID)
	public static GrowthCraftBees instance;

	public static CreativeTabs tab;

	public static BlockTypeDefinition<BlockBeeBox> beeBox;
	public static BlockTypeDefinition<BlockBeeBox> beeBoxBamboo;
	public static BlockTypeDefinition<BlockBeeBox> beeBoxNether;
	public static BlockTypeDefinition<BlockBeeBox> beeBoxThaumcraft;
	public static BlockDefinition beeHive;
	public static GrcBeesItems items = new GrcBeesItems();
	public static GrcBeesBooze booze = new GrcBeesBooze();

	private ILogger logger = new GrcLogger(MOD_ID);
	private GrcBeesConfig config = new GrcBeesConfig();
	private ModuleContainer modules = new ModuleContainer();
	private UserBeesConfig userBeesConfig = new UserBeesConfig();
	private UserFlowersConfig userFlowersConfig = new UserFlowersConfig();
	//private UserHoneyConfig userHoneyConfig = new UserHoneyConfig();

	public static UserBeesConfig getUserBeesConfig()
	{
		return instance.userBeesConfig;
	}

	/**
	 * Only use this logger for logging GrowthCraftBees related items
	 */
	public static ILogger getLogger()
	{
		return instance.logger;
	}

	public static GrcBeesConfig getConfig()
	{
		return instance.config;
	}

	@EventHandler
	public void preload(FMLPreInitializationEvent event)
	{
		config.setLogger(logger);
		config.load(event.getModConfigurationDirectory(), "growthcraft/bees.conf");

		modules.add(items);
		modules.add(booze);

		userBeesConfig.setConfigFile(event.getModConfigurationDirectory(), "growthcraft/bees/bees.json");
		modules.add(userBeesConfig);

		userFlowersConfig.setConfigFile(event.getModConfigurationDirectory(), "growthcraft/bees/flowers.json");
		modules.add(userFlowersConfig);

		//userHoneyConfig.setConfigFile(event.getModConfigurationDirectory(), "growthcraft/bees/honey.json");
		//modules.add(userHoneyConfig);

		if (config.enableGrcBambooIntegration) modules.add(new growthcraft.bees.integration.GrcBambooModule());
		if (config.enableGrcNetherIntegration) modules.add(new growthcraft.bees.integration.GrcNetherModule());
		if (config.enableWailaIntegration) modules.add(new growthcraft.bees.integration.Waila());
		if (config.enableForestryIntegration) modules.add(new growthcraft.bees.integration.ForestryModule());
		if (config.enableThaumcraftIntegration) modules.add(new growthcraft.bees.integration.ThaumcraftModule());

		if (config.debugEnabled)
		{
			BeesRegistry.instance().setLogger(logger);
			modules.setLogger(logger);
		}

		tab = new CreativeTabsGrowthcraftBees();

		initBlocksAndItems();
	}

	private void initBlocksAndItems()
	{
		beeBox  = new BlockTypeDefinition<BlockBeeBox>(new BlockBeeBox());
		beeBox.getBlock().setFlammability(20).setFireSpreadSpeed(5).setHarvestLevel("axe", 0);
		beeHive = new BlockDefinition(new BlockBeeHive());

		modules.preInit();
		register();
	}

	private void register()
	{
		MinecraftForge.EVENT_BUS.register(this);
		// Bee Boxes
		GameRegistry.registerBlock(beeBox.getBlock(), ItemBlockBeeBox.class, "grc.beeBox");
		// Bee Hive(s)
		GameRegistry.registerBlock(beeHive.getBlock(), "grc.beeHive");
		// TileEntities
		GameRegistry.registerTileEntity(TileEntityBeeBox.class, "grc.tileentity.beeBox");
		GameRegistry.registerWorldGenerator(new WorldGeneratorBees(), 0);
		MapGenHelper.registerVillageStructure(ComponentVillageApiarist.class, "grc.apiarist");
		modules.register();
		registerRecipes();
		userBeesConfig.addDefault(items.bee.asStack()).setComment("Growthcraft's default bee");
		BeesRegistry.instance().addHoneyComb(items.honeyCombEmpty.asStack(), items.honeyCombFilled.asStack());
		userFlowersConfig.addDefault(Blocks.red_flower);
		userFlowersConfig.addDefault(Blocks.yellow_flower);
		if (Loader.isModLoaded("BiomesOPlenty"))
		{
			userFlowersConfig.addDefault(
				new UserFlowerEntry("BiomesOPlenty", "flowers", OreDictionary.WILDCARD_VALUE)
					.setEntryType("forced"))
				.setComment("BiomesOPlenty flowers require a forced entry, in order for it to be placed by the bee box spawning.");
			userFlowersConfig.addDefault(
				new UserFlowerEntry("BiomesOPlenty", "flowers2", OreDictionary.WILDCARD_VALUE)
					.setEntryType("forced"))
				.setComment("BiomesOPlenty flowers require a forced entry, in order for it to be placed by the bee box spawning.");
		}
	}

	private void registerRecipes()
	{
		//====================
		// CRAFTING
		//====================
		final BlockDefinition planks = new BlockDefinition(Blocks.planks);
		for (int i = 0; i < 6; ++i)
		{
			GameRegistry.addRecipe(beeBox.asStack(1, i), new Object[] { " A ", "A A", "AAA", 'A', planks.asStack(1, i) });
		}

		final ItemStack honeyStack = items.honeyCombFilled.asStack();
		GameRegistry.addShapelessRecipe(items.honeyJar.asStack(),
			honeyStack, honeyStack, honeyStack, honeyStack, honeyStack, honeyStack, Items.flower_pot);
	}

	private void postRegisterRecipes()
	{
		GameRegistry.addRecipe(new ShapedOreRecipe(beeBox.asStack(), " A ", "A A", "AAA", 'A', "plankWood"));
	}

	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		CommonProxy.instance.initRenders();
		CommonProxy.instance.initSounds();

		NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandlerBees());

		final VillageHandlerBeesApiarist handler = new VillageHandlerBeesApiarist();
		VillagerRegistry.instance().registerVillageCreationHandler(handler);
		VillagerRegistry.instance().registerVillageTradeHandler(GrowthCraftCellar.getConfig().villagerBrewerID, new VillageHandlerBees());
		VillagerRegistry.instance().registerVillageTradeHandler(config.villagerApiaristID, handler);

		CommonProxy.instance.registerVillagerSkin();

		modules.init();
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void onTextureStitchPost(TextureStitchEvent.Post event)
	{
		if (event.map.getTextureType() == 0)
		{
			for (int i = 0; i < booze.honeyMeadBooze.length; ++i)
			{
				booze.honeyMeadBooze[i].setIcons(GrowthCraftCore.liquidSmoothTexture);
			}
		}
	}

	@EventHandler
	public void postload(FMLPostInitializationEvent event)
	{
		postRegisterRecipes();

		modules.postInit();
	}
}
