package net.danyal.parrotbreeding.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParrotEntity.class)
public abstract class ParrotEntityMixin extends TameableEntity implements VariantHolder<ParrotEntity.Variant> {
	@Shadow public abstract boolean isInAir();

	protected ParrotEntityMixin(EntityType<? extends TameableShoulderEntity> entityType, World world, GoalSelector goalSelector, boolean isTamed) {
		super(entityType, world);
    }

	// ADD SUPER.INTERACT MOB BEFORE PARROT SITTING TO PRIORITIZE BREEDING (FROM SUPER METHOD)
	@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/ParrotEntity;isInAir()Z"), cancellable = true)
	private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!this.isInAir() && this.isTamed() && this.isOwner(player)) {
			ActionResult actionResult = super.interactMob(player, hand);
			if (actionResult != ActionResult.SUCCESS_SERVER && actionResult != ActionResult.SUCCESS && actionResult != ActionResult.CONSUME) {
				System.out.println(actionResult);
				if (!this.getWorld().isClient) {
					this.setSitting(!this.isSitting());
					cir.setReturnValue(ActionResult.SUCCESS);
				}
			}
		} else {
			cir.setReturnValue(super.interactMob(player, hand));
		}
	}


	// ADD ANIMAL MATE GOAL
	@Inject(at = @At("HEAD"), method = "initGoals")
	private void addAnimalMateGoal(CallbackInfo ci) {
		this.goalSelector.add(1, new AnimalMateGoal(this, 1.0));
	}

    // ALLOW BABY UPON INITIALIZATION
	@ModifyArg(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PassiveEntity$PassiveData;<init>(Z)V"))
	private boolean modifyInitializeArg(boolean babyAllowed) {
		return true;
    }

	// MAKE PARROT FOOD USABLE FOR BREEDING
	@Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
	private void modifyBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.isIn(ItemTags.PARROT_FOOD)) {
			cir.setReturnValue(true);
		}
	}

	// CREATE AND TAME BABY PARROT ENTITY WITH A VARIANT FROM ONE OF THE PARENTS
	@Inject(method = "createChild", at = @At("HEAD"), cancellable = true)
	private void addParrotChildCreation(ServerWorld world, PassiveEntity entity, CallbackInfoReturnable<PassiveEntity> cir) {
		ParrotEntity parrotEntity = EntityType.PARROT.create(world, SpawnReason.BREEDING);
		if (parrotEntity != null && entity instanceof ParrotEntity parrotEntity2) {
			if (this.random.nextBoolean()) {
				parrotEntity.setVariant(this.getVariant());
			} else {
				parrotEntity.setVariant(parrotEntity2.getVariant());
			}
			if (this.isTamed()) {
				parrotEntity.setOwnerUuid(this.getOwnerUuid());
				parrotEntity.setTamed(true,  true);
			}
		}
		cir.setReturnValue(parrotEntity);
	}


	// INCREASE BABY SOUND PITCH
	@Inject(method = "getSoundPitch()F", at = @At("HEAD"), cancellable = true)
	public void modifySoundPitchForBaby(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	// RETURN TRUE IF BABY
	@Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
	private void modifyIsBaby(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.getBreedingAge() < 0);

	}

	// SET BREEDING CONDITIONS FOR PARROTS
	@Inject(method = "canBreedWith", at = @At("HEAD"), cancellable = true)
	private void modifyCanBreedWith(AnimalEntity other, CallbackInfoReturnable<Boolean> cir) {
			if (other == this) {
				cir.setReturnValue(false);
			} else if (!this.isTamed()) {
				cir.setReturnValue(false);
			} else if (!(other instanceof ParrotEntity parrotEntity)) {
				cir.setReturnValue(false);
			} else if (!parrotEntity.isTamed()) {
				cir.setReturnValue(false);
			} else {
				cir.setReturnValue(!parrotEntity.isSitting() && parrotEntity.isInLove() && this.isInLove());
			}
		}


}