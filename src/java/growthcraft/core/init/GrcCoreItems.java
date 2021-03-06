/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 IceDragon200
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package growthcraft.core.init;

import growthcraft.core.common.definition.ItemDefinition;
import growthcraft.core.common.GrcModuleBase;
import growthcraft.core.common.item.ItemRope;
import growthcraft.core.common.item.ItemSalt;

import cpw.mods.fml.common.registry.GameRegistry;

import net.minecraft.init.Items;
import net.minecraftforge.oredict.OreDictionary;

public class GrcCoreItems extends GrcModuleBase
{
	public ItemDefinition rope;
	public ItemDefinition salt;

	@Override
	public void preInit()
	{
		this.rope = new ItemDefinition(new ItemRope());
		this.salt = new ItemDefinition(new ItemSalt());
	}

	@Override
	public void register()
	{
		GameRegistry.registerItem(rope.getItem(), "grc.rope");
		GameRegistry.registerItem(salt.getItem(), "grccore.salt");
	}

	@Override
	public void init()
	{
		GameRegistry.addRecipe(rope.asStack(8), new Object[] {"A", 'A', Items.lead});

		OreDictionary.registerOre("materialRope", rope.getItem());
		OreDictionary.registerOre("materialSalt", salt.getItem());
		OreDictionary.registerOre("foodSalt", salt.getItem());
	}
}
