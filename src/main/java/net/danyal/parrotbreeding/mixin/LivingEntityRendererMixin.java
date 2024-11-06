package net.danyal.parrotbreeding.mixin;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.ParrotEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin  {
    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at=@At(value = "TAIL"))
    private void updateRenderState(LivingEntity livingEntity, LivingEntityRenderState livingEntityRenderState, float f, CallbackInfo ci) {
        // OVERRIDE BASE SCALE AND AGE SCALE TO 0.5 IF PARROT IS BABY
        if (livingEntity.isBaby() && livingEntity instanceof ParrotEntity) {
            livingEntityRenderState.baseScale = 0.5F;
            livingEntityRenderState.ageScale = 0.5F;
        }
    }
}
