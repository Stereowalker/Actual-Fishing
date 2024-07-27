package com.stereowalker.actualfishing;

import com.stereowalker.unionlib.mod.ClientSegment;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.resources.ResourceLocation;

public class ActuallyFishingClientSegment extends ClientSegment {

	@Override
	public ResourceLocation getModIcon() {
		return VersionHelper.toLoc("actuallyfishing", "textures/icon.png");
	}

}
