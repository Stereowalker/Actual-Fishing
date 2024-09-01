package com.stereowalker.actualfishing;

import java.util.function.Consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.stereowalker.actualfishing.compat.CuriosCompat;
import com.stereowalker.actualfishing.hooks.ModdedHook;
import com.stereowalker.unionlib.api.collectors.InsertCollector;
import com.stereowalker.unionlib.api.collectors.ReloadListeners;
import com.stereowalker.unionlib.mod.MinecraftMod;
import com.stereowalker.unionlib.mod.ServerSegment;
import com.stereowalker.unionlib.util.ModHelper;
import com.stereowalker.unionlib.util.RegistryHelper;
import com.stereowalker.unionlib.util.VersionHelper;
import com.stereowalker.unionlib.world.entity.AccessorySlot;
import com.stereowalker.unionlib.world.item.AccessoryItem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

@Mod("actualfishing")
public class ActualFishing extends MinecraftMod {
	public static final Logger LOGGER = LogManager.getLogger();

	public static ActualFishing instance;
	public ActualFishing() 
	{
		super("actuallyfishing", () -> new ActuallyFishingClientSegment(), () -> new ServerSegment());
		instance = this;
	}
	
	@Override
	public void onModConstruct() {
		if (ModHelper.isCuriosLoaded()) {
			
			NeoForge.EVENT_BUS.addListener((Consumer<ItemFishedEvent>)event->{
				if (event.getHookEntity() instanceof ModdedHook hook && hook.isHookingFish()) {
					event.damageRodBy(1);
				}
			});
			boolean useCurios = false;
			try {Class.forName("top.theillusivec4.curios.api.event.CurioAttributeModifierEvent"); useCurios = true;} 
			catch (Exception e) {System.err.println("Curios support was disabled because the modifier event was not present");}
			if (useCurios) CuriosCompat.load();
		}
	}
	
	@Override
	public void registerServerRelaodableResources(ReloadListeners reloadListener) {
	}

	@Override
	public void registerInserts(InsertCollector collector) {
	}

	/**
	 * Returns an {@link ResourceLocation} namespaced with this mod's modid ("tiered").
	 *
	 * @param path  path of identifier (eg. apple in "minecraft:apple")
	 * @return  ResourceLocation created with a namespace of this mod's modid ("tiered") and provided path
	 */
	public static ResourceLocation id(String path) {
		return VersionHelper.toLoc("actuallyfishing", path);
	}

	public static boolean isPreferredAccessorySlot(ItemStack stack, AccessorySlot slot) {
		if(stack.getItem() instanceof AccessoryItem) {
			AccessoryItem item = (AccessoryItem) stack.getItem();
			return item.getAccessorySlots().contains(slot);
		}

		return false;
	}

	public static boolean isPreferredCurioSlot(ItemStack stack, String slot) {
		return stack.is(TagKey.create(RegistryHelper.itemKey(), VersionHelper.toLoc("curios", slot)));
	}
}
