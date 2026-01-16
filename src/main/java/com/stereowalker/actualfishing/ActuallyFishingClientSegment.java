package com.stereowalker.actualfishing;

import com.stereowalker.unionlib.mod.ClientSegment;
import com.stereowalker.unionlib.util.VersionHelper;

import net.minecraft.resources.Identifier;

public class ActuallyFishingClientSegment extends ClientSegment {

	@Override
	public Identifier getModIcon() {
		return VersionHelper.toLoc("actualfishing", "textures/icon.png");
	}

}
