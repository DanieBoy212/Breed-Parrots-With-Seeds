package net.danyal.parrotbreeding.mixin;

import net.danyal.parrotbreeding.PlayerEntityRenderStateAccessor;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerEntityRendererMixin {

    @Inject(method = "updateRenderState(Lnet/minecraft/client/network/AbstractClientPlayerEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("HEAD"))
    private void setParrotNBTs(AbstractClientPlayerEntity abstractClientPlayerEntity, PlayerEntityRenderState playerEntityRenderState, float f, CallbackInfo ci) {
        if (playerEntityRenderState instanceof PlayerEntityRenderStateAccessor accessor) {
            accessor.breed_Parrots_With_Seeds$setParrotNBTLeft(getParrotNBT(abstractClientPlayerEntity, true));
            accessor.breed_Parrots_With_Seeds$setParrotNBTRight(getParrotNBT(abstractClientPlayerEntity, false));
        }
    }

    @Unique
    private NbtCompound getParrotNBT(AbstractClientPlayerEntity player, boolean isLeft) {
        return isLeft ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();

    }

}
