package com.stereowalker.actualfishing.mixin.aquaculture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.stereowalker.actualfishing.hooks.ModdedHook;
import com.teammetallurgy.aquaculture.api.AquacultureAPI;
import com.teammetallurgy.aquaculture.api.fishing.Hook;
import com.teammetallurgy.aquaculture.api.fishing.Hooks;
import com.teammetallurgy.aquaculture.entity.AquaFishingBobberEntity;
import com.teammetallurgy.aquaculture.item.AquaFishingRodItem;
import com.teammetallurgy.aquaculture.item.BaitItem;
import com.teammetallurgy.aquaculture.misc.AquaConfig;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.util.FakePlayer;

@Mixin(AquaFishingRodItem.class)
public abstract class AquacultureFishingRodItemMixin extends FishingRodItem {
	@Shadow @Final private Tier tier;

	public AquacultureFishingRodItemMixin(Properties pProperties) {
		super(pProperties);
	}

	/**
	 * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see {@link #onItemUse}.
	 */
	@Overwrite
	public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
		ItemStack heldStack = pPlayer.getItemInHand(pHand);
		if (pPlayer instanceof FakePlayer) {
			return InteractionResultHolder.fail(heldStack);
		}
		boolean isAdminRod = (Boolean) AquaConfig.BASIC_OPTIONS.debugMode.get() != false
				&& this.tier == AquacultureAPI.MATS.NEPTUNIUM;
		int damage = this.getDamage(heldStack);
		if (damage >= this.getMaxDamage(heldStack)) {
			return new InteractionResultHolder(InteractionResult.FAIL, (Object) heldStack);
		}
		Hook hook = AquaFishingRodItem.getHookType(heldStack);
		if (pPlayer.fishing != null) {
			if (!pLevel.isClientSide) {
				int currentDamage;
				int retrieve = pPlayer.fishing.retrieve(heldStack);
				if (retrieve >= (currentDamage = this.getMaxDamage(heldStack) - damage)) {
					retrieve = currentDamage;
				}
				if (!isAdminRod) {
					if (hook != Hooks.EMPTY && hook.getDurabilityChance() > 0.0) {
						if (pLevel.random.nextDouble() >= hook.getDurabilityChance()) {
							heldStack.hurtAndBreak(retrieve, (LivingEntity) pPlayer,
									LivingEntity.getSlotForHand((InteractionHand) pHand));
						}
					} else {
						heldStack.hurtAndBreak(retrieve, (LivingEntity) pPlayer,
								LivingEntity.getSlotForHand((InteractionHand) pHand));
					}
				}
			}
			pPlayer.swing(pHand);
			pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE,
					SoundSource.NEUTRAL, 1.0f, 0.4f / (pLevel.random.nextFloat() * 0.4f + 0.8f));
			pPlayer.gameEvent((Holder) GameEvent.ITEM_INTERACT_FINISH);
			//Modification Start
			ModdedHook hook2 = (ModdedHook)pPlayer.fishing;
			if (hook2.isReelingInFish()) {
				pPlayer.startUsingItem(pHand);
				return InteractionResultHolder.consume(heldStack);
			}
			//Mofification End
		} else {
			//Completely replace what vanilla does to allow us to throw the hook
			pPlayer.startUsingItem(pHand);
			return InteractionResultHolder.consume(heldStack);
		}

		return InteractionResultHolder.sidedSuccess(heldStack, pLevel.isClientSide());


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
	public void releaseUsing(ItemStack heldStack, Level level, LivingEntity pEntityLiving, int pTimeLeft) {
		//Copy of what aquaculture does when deploying the hook
		boolean isAdminRod = (Boolean) AquaConfig.BASIC_OPTIONS.debugMode.get() != false
				&& this.tier == AquacultureAPI.MATS.NEPTUNIUM;
		Hook hook = AquaFishingRodItem.getHookType(heldStack);
		if (pEntityLiving instanceof Player player) {
			int duri = this.getUseDuration(heldStack, pEntityLiving) - pTimeLeft;
			System.out.println("Power");
			if (player.fishing == null) {
				level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW,
						SoundSource.NEUTRAL, 0.5f, 0.4f / (level.random.nextFloat() * 0.4f + 0.8f));
				if (level instanceof ServerLevel) {
					ServerLevel serverLevel = (ServerLevel) level;
					int lureSpeed = (int) (EnchantmentHelper.getFishingTimeReduction((ServerLevel) serverLevel,
							(ItemStack) heldStack, (Entity) player) * 20.0f);
					if (this.tier == AquacultureAPI.MATS.NEPTUNIUM) {
						lureSpeed += 100;
					}
					ItemStack bait = AquaFishingRodItem.getBait(heldStack);
					if (!isAdminRod && !bait.isEmpty()) {
						lureSpeed += ((BaitItem) bait.getItem()).getLureSpeedModifier() * 100;
					}
					lureSpeed = Math.min(500, lureSpeed);
					int luck = EnchantmentHelper.getFishingLuckBonus((ServerLevel) serverLevel, (ItemStack) heldStack,
							(Entity) player);
					if (hook != Hooks.EMPTY && hook.getLuckModifier() > 0) {
						luck += hook.getLuckModifier();
					}

					AquaFishingBobberEntity aquaHook = new AquaFishingBobberEntity(player, level, luck, lureSpeed, hook,
							AquaFishingRodItem.getFishingLine(heldStack), AquaFishingRodItem.getBobber(heldStack),
							heldStack);
					//Modification Start
					ModdedHook.throwWithPower(aquaHook, player, duri);
					//Modification End
					level.addFreshEntity(aquaHook);
				}
				player.awardStat(Stats.ITEM_USED.get(this));
				player.gameEvent((Holder) GameEvent.ITEM_INTERACT_START);
			}
		}
	}
}
