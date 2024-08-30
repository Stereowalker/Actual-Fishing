package com.stereowalker.actualfishing.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.stereowalker.actualfishing.hooks.ModdedHook;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

@Mixin(FishingRodItem.class)
public abstract class FishingRodItemMixin extends Item {

	public FishingRodItemMixin(Properties pProperties) {
		super(pProperties);
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see {@link #onItemUse}.
	 */
	@Overwrite
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack itemstack = pPlayer.getItemInHand(pHand);
		if (pPlayer.fishing != null) {
			//Copy of what vanilla does when retrieving the hook
			if (!pLevel.isClientSide) {
				int i = pPlayer.fishing.retrieve(itemstack);
				ItemStack original = itemstack.copy();
				itemstack.hurtAndBreak(i, pPlayer, LivingEntity.getSlotForHand(pHand));
				if(itemstack.isEmpty()) {
				}
			}

			pLevel.playSound(
					null,
					pPlayer.getX(),
					pPlayer.getY(),
					pPlayer.getZ(),
					SoundEvents.FISHING_BOBBER_RETRIEVE,
					SoundSource.NEUTRAL,
					1.0F,
					0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F)
					);
			pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
			//Modification Start
			ModdedHook hook = (ModdedHook)pPlayer.fishing;
			if (hook.isReelingInFish()) {
				pPlayer.startUsingItem(pHand);
				return InteractionResultHolder.consume(itemstack);
			}
			//Mofification End
		} else {
			//Completely replace what vanilla does to allow us to throw the hook
			pPlayer.startUsingItem(pHand);
			return InteractionResultHolder.consume(itemstack);
		}

		return InteractionResultHolder.sidedSuccess(itemstack, pLevel.isClientSide());


	}

	@Override
	public void onUseTick(Level pLevel, LivingEntity pLivingEntity, ItemStack pStack, int pRemainingUseDuration) {
		if (pLivingEntity instanceof Player pPlayer) {
			if (pPlayer.fishing != null) {
				ModdedHook hook = (ModdedHook)pPlayer.fishing;
				hook.continurReelInFish();
			}
		}
	}

	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getUseDuration(ItemStack pStack, LivingEntity p_345962_) {
		return 72000;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack pStack) {
		return UseAnim.BOW;
	}

	@Override
	public void releaseUsing(ItemStack itemstack, Level pLevel, LivingEntity pEntityLiving, int pTimeLeft) {
		if (pEntityLiving instanceof Player pPlayer) {
			int duri = this.getUseDuration(itemstack, pEntityLiving) - pTimeLeft;
			if (pPlayer.fishing == null) {
				//Copy of what vanilla does when deploying the hook
				pLevel.playSound(
						null,
						pPlayer.getX(),
						pPlayer.getY(),
						pPlayer.getZ(),
						SoundEvents.FISHING_BOBBER_THROW,
						SoundSource.NEUTRAL,
						0.5F,
						0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F)
						);
				if (pLevel instanceof ServerLevel serverlevel) {
					int j = (int)(EnchantmentHelper.getFishingTimeReduction(serverlevel, itemstack, pPlayer) * 20.0F);
					int k = EnchantmentHelper.getFishingLuckBonus(serverlevel, itemstack, pPlayer);
					FishingHook hook = new FishingHook(pPlayer, pLevel, k, j);
					//Modification Start
					ModdedHook.throwWithPower(hook, pPlayer, duri);
					//Modification End
					pLevel.addFreshEntity(hook);
				}

				pPlayer.awardStat(Stats.ITEM_USED.get(this));
				pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
			}
		}
	}
}
