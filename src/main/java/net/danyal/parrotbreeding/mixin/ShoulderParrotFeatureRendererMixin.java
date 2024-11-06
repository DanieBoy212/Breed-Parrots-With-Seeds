package net.danyal.parrotbreeding.mixin;

import net.danyal.parrotbreeding.PlayerEntityRenderStateAccessor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShoulderParrotFeatureRenderer.class)
public abstract class ShoulderParrotFeatureRendererMixin {
    @Shadow private void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, PlayerEntityRenderState state, ParrotEntity.Variant parrotVariant, float headYaw, float headPitch, boolean left) {}

    // INCORPORATE SIZE DIFFERENCE FOR BABY PARROTS ON SHOULDER WHEN RENDERING
    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/PlayerEntityRenderState;FF)V", at = @At(value = "HEAD"), cancellable = true)
    public void renderParrot(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, PlayerEntityRenderState playerEntityRenderState, float f, float g, CallbackInfo ci) {
        ParrotEntity.Variant variant = playerEntityRenderState.leftShoulderParrotVariant;
        ParrotEntity.Variant variant2 = playerEntityRenderState.rightShoulderParrotVariant;
        NbtCompound parrotNBTLeft, parrotNBTRight;

        // USE ACCESSOR METHODS FROM THE INTERFACE USED TO ACCESS PLAYERS PARROT NBT COMPOUNDS TO DETERMINE AGE
        if (playerEntityRenderState instanceof PlayerEntityRenderStateAccessor accessor) {
            parrotNBTLeft = accessor.breed_Parrots_With_Seeds$getParrotNBTLeft();
            parrotNBTRight = accessor.breed_Parrots_With_Seeds$getParrotNBTRight();

            // LEFT PARROT
            if (variant != null) {
                if (parrotNBTLeft.getInt("Age") < 0) {
                    matrixStack.scale(0.5F, 0.5F, 0.5F);
                    matrixStack.translate(0.35F, playerEntityRenderState.isInSneakingPose ? 0.15F : 0, 0);
                }

                this.render(matrixStack, vertexConsumerProvider, i, playerEntityRenderState, variant, f, g, true);

                // RESET THE TRANSLATIONS AND SCALE OF THE MATRICES THAT WERE APPLIED IF THE PARROT WAS A BABY
                // ENSURES NORMAL RENDING OF BOTH AN ADULT AND BABY PARROT TOGETHER ON THE PLAYER
                if (parrotNBTLeft.getInt("Age") < 0) {
                    matrixStack.translate(-0.35F, 0, 0);
                    matrixStack.scale(2F, 2F, 2F);
                }
            }

            // RIGHT PARROT
            if (variant2 != null) {
                if (parrotNBTRight.getInt("Age") < 0) {
                    matrixStack.scale(0.5F, 0.5F, 0.5F);
                    matrixStack.translate(-0.35F, 0, 0);
                }
                this.render(matrixStack, vertexConsumerProvider, i, playerEntityRenderState, variant2, f, g, false);

                // RESET MATRICES AGAIN
                if (parrotNBTRight.getInt("Age") < 0) {
                    matrixStack.scale(2F, 2F, 2F);
                    matrixStack.translate(0.35F, 0, 0);
                }
            }
        }

        ci.cancel();
    }



}
