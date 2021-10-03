/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.tools.powered;


import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.sync.GuiBridge;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.contents.PortableCellViewer;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;
import com.google.common.base.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.RecipeSorter;
import java.util.*;
import java.util.function.Predicate;


public class ToolPortableCell extends AEBasePoweredItem implements IStorageCell, IGuiItem, IItemGroup
{
	public static final int maxLevel = 3;

	public static final int[] tierCapacity = new int[] { 512, 2048, 8192, 32768 };
	public static final int[] tierBytesPerType = new int[] { 8, 32, 128, 512 };

	@SuppressWarnings("unchecked")
	public ToolPortableCell()
	{
		super( AEConfig.instance.portableCellBattery, Optional.<String>absent() );
		this.setFeature( EnumSet.of( AEFeature.PortableCell, AEFeature.StorageCells, AEFeature.PoweredTools ) );

		CraftingManager.getInstance().getRecipeList().add(new ToolPortableCell.Recipe());
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer player )
	{
		Platform.openGUI( player, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_PORTABLE_CELL );
		return item;
	}

	@SideOnly( Side.CLIENT )
	@Override
	public boolean isFull3D()
	{
		return false;
	}

	@Override
	public void addCheckedInformation( final ItemStack stack, final EntityPlayer player, final List<String> lines, final boolean displayMoreInfo )
	{
		super.addCheckedInformation( stack, player, lines, displayMoreInfo );

		final IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( stack, null, StorageChannel.ITEMS );

		if( cdi instanceof CellInventoryHandler )
		{
			final ICellInventory cd = ( (ICellInventoryHandler) cdi ).getCellInv();
			if( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalBytes() + ' ' + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + ' ' + cd.getTotalItemTypes() + ' ' + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public double getAEMaxPower(ItemStack is) {
		int meta = getMeta(is);

		return super.getAEMaxPower(is) * (meta < 2 ? 1 : 8);
	}

	@Override
	public int getBytes( final ItemStack cellItem )
	{
		return tierCapacity[getMeta(cellItem)];
	}

	@Override
	public int BytePerType( final ItemStack cellItem )
	{
		return tierBytesPerType[getMeta(cellItem)];
	}

	@Override
	public int getBytesPerType( final ItemStack cellItem )
	{
		return tierBytesPerType[getMeta(cellItem)];
	}

	private int getMeta(ItemStack cellItem) {
		int meta = Items.feather.getDamage(cellItem);
		return Math.min(meta, maxLevel);
	}

	@Override
	public int getTotalTypes( final ItemStack cellItem )
	{
		return 27;
	}

	@Override
	public boolean isBlackListed( final ItemStack cellItem, final IAEItemStack requestedAddition )
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

	@Override
	public boolean isStorageCell( final ItemStack i )
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public String getUnlocalizedGroupName( final Set<ItemStack> others, final ItemStack is )
	{
		return GuiText.StorageCells.getUnlocalized();
	}

	@Override
	public boolean isEditable( final ItemStack is )
	{
		return true;
	}

	@Override
	public IInventory getUpgradesInventory( final ItemStack is )
	{
		return new CellUpgrades( is, 2 );
	}

	@Override
	public IInventory getConfigInventory( final ItemStack is )
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode( final ItemStack is )
	{
		final String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch( final Throwable t )
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode( final ItemStack is, final FuzzyMode fzMode )
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public IGuiItemObject getGuiObject( final ItemStack is, final World w, final int x, final int y, final int z )
	{
		return new PortableCellViewer( is, x );
	}


	@Override
	public String getOreFilter(ItemStack is) {
		return Platform.openNbtData( is ).getString( "OreFilter" );
	}

	@Override
	public void setOreFilter(ItemStack is, String filter) {
		Platform.openNbtData( is ).setString("OreFilter", filter);
	}

	@Override
	public int getColorFromItemStack(ItemStack is, int meta) {
		NBTTagCompound nbt = is.getTagCompound();

		int color = super.getColorFromItemStack(is, meta);

		if (nbt != null && nbt.hasKey("color")) {
			color = nbt.getInteger("color");
		}

		return color;
	}

	private final static IIcon[] icons = new IIcon[maxLevel + 1];

	@Override
	public void registerIcons(IIconRegister register) {
		for (int i = 0; i <= maxLevel; i++) {
			icons[i] = register.registerIcon("appliedenergistics2:ToolPortableCell." + i);
		}
	}

	@Override
	public IIcon getIconFromDamage(int damage) {
		return icons[Math.min(maxLevel, damage)];
	}

	@Override
	protected void getCheckedSubItems(Item sameItem, CreativeTabs creativeTab, List<ItemStack> itemStacks) {
		for (int i = 0; i <= maxLevel; i++) {
			final ItemStack charged = new ItemStack( this, 1, i );
			final NBTTagCompound tag = Platform.openNbtData( charged );

			tag.setDouble( "internalCurrentPower", this.getAEMaxPower( charged ) );
			tag.setDouble( "internalMaxPower", this.getAEMaxPower( charged ) );

			itemStacks.add( charged );
			itemStacks.add( new ItemStack( this, 1, i ) );
		}
	}

	public static class Recipe implements IRecipe {

		private ItemStack cache = null;

		private int countFullStacks (InventoryCrafting ic) {
			int count = 0;

			for (int i = 0; i < ic.getSizeInventory(); ++i) {
				ItemStack itemstack = ic.getStackInSlot(i);

				if (itemstack != null) {
					count++;
				}
			}

			return count;
		}

		private ItemStack getStack (InventoryCrafting ic, Predicate<ItemStack> validator) {
			for (int i = 0; i < ic.getSizeInventory(); ++i) {
				ItemStack itemstack = ic.getStackInSlot(i);

				if (itemstack != null && validator.test(itemstack)) {
					return itemstack;
				}
			}

			return null;
		}

		static Map<String, String> oreToDyeName = new HashMap<>();
		static Map<String, Integer> dyeNameToColor = new HashMap<>();
		static {
			for (int i = 0; i < ItemDye.field_150923_a.length; i++) {
				String name = ItemDye.field_150923_a[i];
				String oreDict = "dyeLightGray";
				if (!name.equals("silver")) {
					oreDict = "dye" + name.substring(0, 1).toUpperCase() + name.substring(1);
				}
				oreToDyeName.put(oreDict, ItemDye.field_150921_b[i]);
			}

			for (int i = 0; i < ItemDye.field_150921_b.length; i++) {
				dyeNameToColor.put(ItemDye.field_150921_b[i], ItemDye.field_150922_c[i]);
			}
		}

		public int getColor(ItemStack stack) {
			String name = Arrays.stream(OreDictionary.getOreIDs(stack))
					.mapToObj(OreDictionary::getOreName)
					.map(oreToDyeName::get)
					.filter(Objects::nonNull)
					.findAny().orElse(null);
			if (name == null) {
				return -1;
			}
			return dyeNameToColor.get(name);
		}


		@Override
		public boolean matches(InventoryCrafting ic, World world) {
			if (countFullStacks(ic) != 2) {
				return false;
			}

			ItemStack dye = getStack(ic, stack -> getColor(stack) >= 0);
			ItemStack cell = getStack(ic, stack -> stack.getItem() instanceof ToolPortableCell);

			if (dye == null || cell == null) {
				return false;
			}

			cache = cell.copy();

			NBTTagCompound nbt = cache.getTagCompound();
			nbt = nbt == null ? new NBTTagCompound() : nbt;
			nbt.setInteger("color", getColor(dye));
			cache.setTagCompound(nbt);

			return true;
		}

		@Override
		public ItemStack getCraftingResult(InventoryCrafting p_77572_1_) {
			return cache == null ? null : cache.copy();
		}

		@Override
		public int getRecipeSize() {
			return 10;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return cache;
		}

		static {
			RecipeSorter.register("ToolPortableCellColored", ToolPortableCell.Recipe.class, RecipeSorter.Category.SHAPELESS, "");
		}
	}
}
