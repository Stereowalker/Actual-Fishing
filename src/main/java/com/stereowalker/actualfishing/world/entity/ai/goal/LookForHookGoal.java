package com.stereowalker.actualfishing.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.stereowalker.actualfishing.hooks.LurableFish;
import com.stereowalker.actualfishing.hooks.ModdedHook;
import com.stereowalker.actualfishing.hooks.LurableFish.LureState;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.phys.AABB;

public class LookForHookGoal extends Goal {
    protected final AbstractFish mob;
    private final double speedModifier;
    private double px;
    private double py;
    private double pz;
    private double pRotX;
    private double pRotY;
    @Nullable
    protected FishingHook fishHook;
    private int calmDown;
    private boolean isRunning;
    private final boolean canScare;

    public LookForHookGoal(AbstractFish pMob, double pSpeedModifier, boolean pCanScare) {
        this.mob = pMob;
        this.speedModifier = pSpeedModifier;
        this.canScare = pCanScare;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.calmDown > 0) {
            this.calmDown--;
            return false;
        } else {
        	List<FishingHook> hooks = this.mob.level().getEntitiesOfClass(FishingHook.class, AABB.ofSize(this.mob.position(), 100, 100, 100));
            hooks.removeIf((fHook) -> fHook.getHookedIn() != null);
        	this.fishHook = hooks.size() > 0 ? hooks.get(0) : null;
            return this.fishHook != null;
        }
    }

    private boolean shouldFollow(LivingEntity p_148139_) {
    	return true;
//        return this.items.test(p_148139_.getMainHandItem()) || this.items.test(p_148139_.getOffhandItem());
    }

    @Override
    public boolean canContinueToUse() {
        if (this.canScare()) {
            if (this.mob.distanceToSqr(this.fishHook) < 36.0) {
//                if (this.hook.distanceToSqr(this.px, this.py, this.pz) > 0.010000000000000002) {
//                    return false;
//                }
//
//                if (Math.abs((double)this.hook.getXRot() - this.pRotX) > 5.0 || Math.abs((double)this.hook.getYRot() - this.pRotY) > 5.0) {
//                    return false;
//                }
            } else {
                this.px = this.fishHook.getX();
                this.py = this.fishHook.getY();
                this.pz = this.fishHook.getZ();
            }

            this.pRotX = (double)this.fishHook.getXRot();
            this.pRotY = (double)this.fishHook.getYRot();
        }

        return this.canUse();
    }

    protected boolean canScare() {
        return this.canScare;
    }

    @Override
    public void start() {
        this.px = this.fishHook.getX();
        this.py = this.fishHook.getY();
        this.pz = this.fishHook.getZ();
        if (mob instanceof LurableFish fish && fishHook instanceof ModdedHook hook) {
        	float odds = hook.oddsToBite();
        	if (mob.level().getDayTime() % 24000 > 18000) odds -= .3f;
        	odds = Mth.clamp(odds, 0, 1);
        	if (this.mob.level().getRandom().nextFloat() < odds) {
        		fish.setWillBite(LureState.WILL_BITE);
        	}
        	else {
        		if (this.mob.level().getRandom().nextInt(2) == 0) 
        			fish.setWillBite(LureState.WILL_APPROACH_BUT_NOT_BITE);
        		else
        			fish.setWillBite(LureState.WILL_IGNORE_HOOK);
        	}
        }
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.fishHook = null;
        this.mob.getNavigation().stop();
        this.calmDown = reducedTickDelay(100);
        if (mob instanceof LurableFish fish) {
        	fish.setWillBite(LureState.NOT_DECIDED);
        }
        this.isRunning = false;
    }

    @Override
    public void tick() {
    	if (mob instanceof LurableFish fish && fishHook.getHookedIn() == null && mob.isInWater()) {
    		if (fish.lureDecision() == LureState.WILL_IGNORE_HOOK) {
    			this.calmDown = reducedTickDelay(200);
            	this.mob.getNavigation().stop();
    		}
    		else {
        		double stopDistanceFromHook = fish.lureDecision() == LureState.WILL_BITE ? 1.2 : 6.25;
            	this.mob.getLookControl().setLookAt(this.fishHook, (float)(this.mob.getMaxHeadYRot() + 20), (float)this.mob.getMaxHeadXRot());
            	if (this.mob.distanceToSqr(this.fishHook) < stopDistanceFromHook) {
            		this.mob.getNavigation().stop();
            		if (fish.lureDecision() == LureState.WILL_BITE) {
            			((ModdedHook)this.fishHook).hookFish(this.mob);
            		}
            		else {
            			this.calmDown = reducedTickDelay(100);
            		}
            	} else {
            		this.mob.getNavigation().moveTo(this.fishHook, this.speedModifier);
            	}
    		}
        } else {
			this.calmDown = reducedTickDelay(20);
        	this.mob.getNavigation().stop();
        }
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}
