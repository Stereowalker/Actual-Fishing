package com.stereowalker.actualfishing.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModifierUtils {
	/**
	 * Returns the ID of a random attribute that is valid for the given {@link Item} in {@link ResourceLocation} form.
	 * <p> If there is no valid attribute for the given {@link Item}, null is returned.
	 *
	 * @param item  {@link Item} to generate a random attribute for
	 * @return  id of random attribute for item in {@link ResourceLocation} form, or null if there are no valid options
	 */
	public static ResourceLocation getRandomAttributeIDFor(Item item) {
		return null;
	}

	private ModifierUtils() {
		// no-op
	}
}