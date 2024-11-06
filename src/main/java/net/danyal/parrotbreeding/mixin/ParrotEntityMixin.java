package net.danyal.parrotbreeding.mixin;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParrotEntity.class)
public abstract class ParrotEntityMixin extends TameableEntity implements VariantHolder<ParrotEntity.Variant> {
	@Unique
	private boolean isBabyParrot = false; // Default to false for adult parrots

	@Shadow public abstract ParrotEntity.Variant getVariant();

	@Shadow public abstract boolean isBaby();

	@Shadow public abstract float getSoundPitch();

	@Shadow protected abstract EntityNavigation createNavigation(World world);

	@Shadow public abstract boolean isInAir();

	protected ParrotEntityMixin(EntityType<? extends TameableShoulderEntity> entityType, World world, GoalSelector goalSelector, boolean isTamed) {
		super(entityType, world);
    }

	// To the parrots interactMob method, add super.interactMob before the parrots sitting function
	// This is because super.interactMob handles mob breeding, which should be prioritized over making the parrot sit
	@Inject(method = "interactMob", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/ParrotEntity;isInAir()Z"), cancellable = true)
	private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (!this.isInAir() && this.isTamed() && this.isOwner(player)) {
			ActionResult actionResult = super.interactMob(player, hand);
			if (actionResult != ActionResult.SUCCESS_SERVER && actionResult != ActionResult.SUCCESS && actionResult != ActionResult.CONSUME) {
				if (!this.getWorld().isClient) {
					this.setSitting(!this.isSitting());
				}
			}

			cir.setReturnValue(ActionResult.SUCCESS);
		} else {
			cir.setReturnValue(super.interactMob(player, hand));
		}
	}


	// When getting parrots base dimensions, ensure dimensions are scaled by the above scale factor, as it will account for
	// whether the parrot is a baby or not
	@Override
	public EntityDimensions getBaseDimensions(EntityPose pose) {
		return this.getType().getDimensions().scaled(this.getScaleFactor());
	}


	// Add the animal mate goal when the parrot is initialized
	@Inject(at = @At("HEAD"), method = "initGoals")
	private void addAnimalMateGoal(CallbackInfo ci) {
		this.goalSelector.add(1, new AnimalMateGoal(this, 1.0));
	}

    // Change the babyAllowed argument in the entities passive data to true, to allow baby parrots
	@ModifyArg(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/passive/PassiveEntity$PassiveData;<init>(Z)V"))
	private boolean modifyInitializeArg(boolean babyAllowed) {
		return true;
    }

	// Modify the isBreedingItem method so that if the players held item is tagged with parrot food, it is a usable food
	// for breeding
	@Inject(method = "isBreedingItem", at = @At("HEAD"), cancellable = true)
	private void modifyBreedingItem(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (stack.isIn(ItemTags.PARROT_FOOD)) {
			cir.setReturnValue(true);
		}
	}

	// Create the parrot entity within the current world, assign the baby parrot a random variant from either parrot,
	// and automatically tame the baby parrot
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


	// Modify the sound pitch so that baby parrots have a higher pitched sound, like other baby mobs
	@Inject(method = "getSoundPitch()F", at = @At("HEAD"), cancellable = true)
	public void modifySoundPitchForBaby(CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(this.isBaby() ? (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 2.5F : (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
	}

	// Modify the return value of isBaby to actually determine if the parrot is a baby, using the parrots breeding age
	@Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
	private void modifyIsBaby(CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(this.getBreedingAge() < 0);

	}

	// Modify the conditions under which a parrot can breed another parrot - almost identical to the code found in WolfEntity.java
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