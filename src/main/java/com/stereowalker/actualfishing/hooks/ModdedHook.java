package com.stereowalker.actualfishing.hooks;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public interface ModdedHook {
	public void hookFish(AbstractFish fish);
	public boolean isHookingFish();
	public boolean isReelingInFish();
	public void reelInFish();
	public void continurReelInFish();
	public float fishingLineDistance();
	public float maximumLineDistance();
	
	default float lineBreakDistance() {
		return 20f;
	}
	
	public static void throwWithPower(FishingHook hook, Player pPlayer, int time) {
		float pow = getPowerForTime(time);
		System.out.println("POW "+pow);
		float f = pPlayer.getXRot();
        float f1 = pPlayer.getYRot();
        float f2 = Mth.cos(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float f3 = Mth.sin(-f1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float f4 = -Mth.cos(-f * (float) (Math.PI / 180.0));
        float f5 = Mth.sin(-f * (float) (Math.PI / 180.0));
        double d0 = pPlayer.getX() - (double)f3 * 0.3;
        double d1 = pPlayer.getEyeY();
        double d2 = pPlayer.getZ() - (double)f2 * 0.3;
        hook.moveTo(d0, d1, d2, f1, f);
        Vec3 vec3 = new Vec3((double)(-f3), (double)Mth.clamp(-(f5 / f4), -5.0F, 5.0F), (double)(-f2));
        double d3 = vec3.length();
        vec3 = vec3.multiply(
            0.6 / d3 + hook.getRandom().triangle(0.5, 0.0103365), 0.6 / d3 + hook.getRandom().triangle(0.5, 0.0103365), 0.6 / d3 + hook.getRandom().triangle(0.5, 0.0103365)
        );
        hook.setDeltaMovement(vec3.scale(pow));
        hook.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float)Math.PI));
        hook.setXRot((float)(Mth.atan2(vec3.y, vec3.horizontalDistance()) * 180.0F / (float)Math.PI));
        hook.yRotO = hook.getYRot();
        hook.xRotO = hook.getXRot();
	}

    public static float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }
	
	default float oddsToBite() {
		float possibleMaxLureSpeedMod = 600f;
		float lureSpeedMod = (float)((FishingHook & ModdedHook)this).lureSpeed / possibleMaxLureSpeedMod;
		return .3f + (lureSpeedMod * .7f);
	}
	
	static <E extends FishingHook & ModdedHook> boolean tickOverrideWithoutFish(E hook) {
		float f = hook.distanceTo(hook.getPlayerOwner());
		if (f > hook.maximumLineDistance()) {
			hook.elasticRangeLineBehaviour(hook.getPlayerOwner(), hook, f);
		}
		return false;
	}
	
	static <E extends FishingHook & ModdedHook> void tickHookWithFish(E hook) {
//        Leashable.LeashData leashable$leashdata = p_352082_.getLeashData();
//        if (leashable$leashdata != null && leashable$leashdata.delayedLeashInfo != null) {
//            restoreLeashFromSave(p_352082_, leashable$leashdata);
//        }
//
//        if (leashable$leashdata != null && leashable$leashdata.leashHolder != null) {
		Entity angler = hook.getPlayerOwner();
		Entity fish = hook.getHookedIn();
		if (!angler.isAlive() || !fish.isAlive()) {
			hook.discard();
		}

		if (angler != null && angler.level() == hook.level()) {
			float f = fish.distanceTo(angler);
//                if (!p_352082_.handleLeashAtDistance(entity, f)) {
//                    return;
//                }

			if ((double)f > hook.lineBreakDistance()) {
				hook.hookTooFarBehaviour();
			} else if ((double)f > hook.maximumLineDistance()) {
				hook.elasticRangeLineBehaviour(angler, fish, f);
				fish.checkSlowFallDistance();
			} else {
//                    p_352082_.closeRangeLeashBehaviour(entity);
			}
			hook.setPos(fish.position());
		}
//        }
    }
	
    default void hookTooFarBehaviour() {
    	Player player = ((FishingHook & ModdedHook)this).getPlayerOwner();
    	if (player != null) {
    		ItemStack itemstack = player.getMainHandItem();
    		ItemStack itemstack1 = player.getOffhandItem();
    		boolean flag = itemstack.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST);
    		boolean flag1 = itemstack1.canPerformAction(net.neoforged.neoforge.common.ItemAbilities.FISHING_ROD_CAST);
    		if (flag) {
    			itemstack.hurtAndBreak(5, player, LivingEntity.getSlotForHand(InteractionHand.MAIN_HAND));
    			if(itemstack.isEmpty()) {
//                    net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, original, InteractionHand.MAIN_HAND);
                }
    		} else if (flag1) {
    			itemstack1.hurtAndBreak(5, player, LivingEntity.getSlotForHand(InteractionHand.OFF_HAND));
    			if(itemstack1.isEmpty()) {
//                    net.neoforged.neoforge.event.EventHooks.onPlayerDestroyItem(player, original, InteractionHand.OFF_HAND);
                }
    		}
    	}
    	((FishingHook & ModdedHook)this).discard();
    }


    default void elasticRangeLineBehaviour(Entity angler, Entity fish, float distance) {
    	Vec3 vec3 = angler.position().subtract(fish.position()).normalize().scale((double)distance - maximumLineDistance());
        Vec3 vec31 = fish.getDeltaMovement();
        boolean flag = vec31.dot(vec3) > 0.0;
        fish.setDeltaMovement(vec31.add(vec3.scale(flag ? 0.15F : 0.2F)));
    }
}
