package com.stereowalker.actualfishing.mixin.aquaculture;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stereowalker.actualfishing.hooks.ModdedHook;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;

import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

@Mixin(AquaFishingBobberEntity.class)
public abstract class AquacultureFishingHookMixin extends FishingHook implements ModdedHook {
	public AquacultureFishingHookMixin(EntityType<? extends FishingHook> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void followFish(CallbackInfo ci) {
		FishingHook hook = (FishingHook)(Object)this;
		if (getHookedIn() == null) {
			float f = hook.distanceTo(getPlayerOwner());
			if (f > maximumLineDistance()) {
				elasticRangeLineBehaviour(getPlayerOwner(), this, f);
			}
		}
		else if (getHookedIn() instanceof AbstractFish) {
			ModdedHook.tickHookWithFish((FishingHook & ModdedHook)hook);
			ci.cancel();
		}
	}

	//This only works cuz on neoforged. use the above event to achieve the same results without it
	@Redirect(method = "retrieve", at = @At(value = "NEW", target = "net/neoforged/neoforge/event/entity/player/ItemFishedEvent"))
	private net.neoforged.neoforge.event.entity.player.ItemFishedEvent getRandomItems(List<ItemStack> stacks, int rodDamage, FishingHook hook) {
		stacks.removeIf(stack -> stack.is(ItemTags.FISHES));
		return new net.neoforged.neoforge.event.entity.player.ItemFishedEvent(stacks, rodDamage, hook);
	}

	@Inject(method = "retrieve", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"))
	public void retrieve(ItemStack pStack, CallbackInfoReturnable<Integer> cir) {
		Player player = this.getPlayerOwner();
        if (getHookedIn() instanceof AbstractFish && isHookingFish() && !getHookedIn().isRemoved()) {
        	if (isReelingInFish()) {
            	cir.setReturnValue(0);
        	}
        	else {
            	player.awardStat(Stats.FISH_CAUGHT, 1);
            	reelInFish();
            	cir.setReturnValue(1);
        	}
        }
	}
	
	//Aquaculture special
	@Shadow private ItemStack fishingRod;
}
