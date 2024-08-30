package com.stereowalker.actualfishing.mixin;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.stereowalker.actualfishing.hooks.ModdedHook;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.FishingHook.FishHookState;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile implements ModdedHook {
	@Shadow private void setHookedEntity(@Nullable Entity pHookedEntity) {}
	@Shadow public Player getPlayerOwner() {return null;}
	@Shadow public Entity getHookedIn() {return null;}
	@Shadow private boolean shouldStopFishing(Player pPlayer) {return false;}
	@Shadow private FishingHook.FishHookState currentState;
	@Shadow private int lureSpeed;
	
	private boolean fishBitHook = false;
	private boolean pullingFish = false;
	private float lineDistance = 14f;
	
	protected FishingHookMixin(EntityType<? extends Projectile> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}
	
	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	public void followFish(CallbackInfo ci) {
		FishingHook hook = (FishingHook)(Object)this;
		if (getHookedIn() == null) {
			if (ModdedHook.tickOverrideWithoutFish((FishingHook & ModdedHook)hook)) {
				ci.cancel();
			}
		}
		else if (getHookedIn() instanceof AbstractFish) {
			ModdedHook.tickHookWithFish((FishingHook & ModdedHook)hook);
			ci.cancel();
		}
	}
	
	@Inject(method = "pullEntity", at = @At("HEAD"), cancellable = true)
	public void pull(Entity pEntity, CallbackInfo ci) {
		Entity entity = this.getOwner();
		if (pEntity instanceof AbstractFish fish && entity != null && fishBitHook) {
			if (!isReelingInFish()) {
				if (getPlayerOwner() != null) {
					fish.hurt(this.damageSources().playerAttack(getPlayerOwner()), fish.getHealth() * .4f);
				}
				Vec3 vec3 = new Vec3(entity.getX() - this.getX(), ((entity.getY() + 1.8f) - this.getY()) * 2, entity.getZ() - this.getZ()).multiply(0.04f, 0.08f, 0.04f);
				pEntity.setDeltaMovement(pEntity.getDeltaMovement().add(vec3));
			}
			ci.cancel();
		}
	}
	
	@Redirect(method = "retrieve", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootParams;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
	private ObjectArrayList<ItemStack> getRandomItems(LootTable ta, LootParams pParams) {
		ObjectArrayList<ItemStack> stacks = ta.getRandomItems(pParams);
		stacks.removeIf(stack -> stack.is(ItemTags.FISHES));
		return stacks;
	}
	
//	@ModifyVariable(method = "retrieve", print = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;broadcastEntityEvent(Lnet/minecraft/world/entity/Entity;B)V"/*
//																																		 * ,
//																																		 * ordinal
//																																		 * =
//																																		 * 2,
//																																		 * opcode
//																																		 * =
//																																		 * Opcodes
//																																		 * .
//																																		 * GETFIELD
//																																		 */))
//	public int retrieve(int i) {
//		return 1;
//	}

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

	
	//Modded Hook
	@Override
	public void hookFish(AbstractFish fish) {
		this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
		setHookedEntity(fish);
		if (getPlayerOwner() != null) {
			fish.hurt(this.damageSources().playerAttack(getPlayerOwner()), fish.getHealth() * .1f);
		}
		this.setDeltaMovement(Vec3.ZERO);
		currentState = FishHookState.HOOKED_IN_ENTITY;
		fishBitHook = true;
	}
	
	@Override
	public boolean isHookingFish() {
		return fishBitHook;
	}
	
	@Override
	public boolean isReelingInFish() {
		return pullingFish;
	}
	
	@Override
	public void reelInFish() {
		pullingFish = true;;
	}
	
	@Override
	public void continurReelInFish() {
		if (lineDistance > .4f) lineDistance -= .15f;
		else lineDistance = .4f;
	}
	
	@Override
	public float maximumLineDistance() {
		return lineDistance;
	}
}
