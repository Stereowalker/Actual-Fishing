package com.stereowalker.actualfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.stereowalker.actualfishing.hooks.LurableFish;
import com.stereowalker.actualfishing.world.entity.ai.goal.AvoidHookGoal;
import com.stereowalker.actualfishing.world.entity.ai.goal.LookForHookGoal;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;

@Mixin(AbstractFish.class)
public abstract class AbstractFishMixin extends WaterAnimal implements LurableFish {
	LureState bite = LureState.NOT_DECIDED;
	protected AbstractFishMixin(EntityType<? extends WaterAnimal> pEntityType, Level pLevel) {
		super(pEntityType, pLevel);
	}

	@Inject(method = "registerGoals", at = @At("TAIL"))
	public void retrieve(CallbackInfo ci) {
		this.goalSelector.addGoal(-1, new AvoidHookGoal((AbstractFish)(Object)this, 10.0F, 1.6, 1.4));
		this.goalSelector.addGoal(1, new LookForHookGoal((AbstractFish)(Object)this, 0.55, true));
	}
	
	@Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
	public void addAdditionalSaveData_inject(CompoundTag pCompound, CallbackInfo ci) {
        pCompound.putInt("WillBite", this.lureDecision().ordinal());
	}
	
	@Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
	public void readAdditionalSaveData_inject(CompoundTag pCompound, CallbackInfo ci) {
        this.setWillBite(LureState.values()[pCompound.getInt("WillBite")]);
	}
	
    @Override
    public LureState lureDecision() {
    	return bite;
    }
    
    @Override
    public void setWillBite(LureState arg0) {
    	bite = arg0;
    }
}
