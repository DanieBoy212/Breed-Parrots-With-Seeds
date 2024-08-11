package net.danyal.parrotbreeding.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShoulderParrotFeatureRenderer.class)
public class ShoulderParrotFeatureRendererMixin<T extends PlayerEntity> {
    @Unique
    private boolean leftShoulder;

    // Get which shoulder parrot is being rendered
    // This will be used to determine with parrot to modify the size and position of, if they are a baby
    @Inject(method = "renderShoulderParrot",at = @At(value = "HEAD"))
    private void getLeftShoulder(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T player, float limbAngle, float limbDistance, float headYaw, float headPitch, boolean leftShoulder, CallbackInfo ci){
        this.leftShoulder = leftShoulder;
    }

    // Inject the lambda method that pushes the matrices used to render a parrot on a shoulder
    // Using the parrots nbt compound we can get from the players getShoulderEntity() methods, we can determine the parrots age
    // Check each side to see if a parrot exists there, and if that parrots age is below zero (indicates baby)
    // If so, scale down and translate the matrices to make the baby parrot appear smaller and in the right position
    @Inject(method = "method_17958", at = @At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack.push ()V", shift = At.Shift.AFTER, by = 1))
    private void scaleAndTranslateBabyParrotMatrices(MatrixStack matrixStack, boolean bl, PlayerEntity playerEntity, NbtCompound nbtCompound, VertexConsumerProvider vertexConsumerProvider, int i, float f, float g, float h, float j, EntityType type, CallbackInfo ci){
        NbtCompound parrotNbtCompoundLeft = playerEntity.getShoulderEntityLeft();
        NbtCompound parrotNbtCompoundRight = playerEntity.getShoulderEntityRight();

        if (this.leftShoulder && parrotNbtCompoundLeft.getInt("Age") < 0) {
            matrixStack.scale(0.5F,0.5F,0.5F);
            matrixStack.translate(0.35F, 0F,0F);
        }

        if (!this.leftShoulder && parrotNbtCompoundRight.getInt("Age") < 0) {
            matrixStack.scale(0.5F,0.5F,0.5F);
            matrixStack.translate(-0.35F, 0F,0F);
        }

    }
}
