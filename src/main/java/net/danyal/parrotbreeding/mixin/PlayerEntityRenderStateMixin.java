package net.danyal.parrotbreeding.mixin;

import net.danyal.parrotbreeding.PlayerEntityRenderStateAccessor;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerEntityRenderState.class)
public abstract class PlayerEntityRenderStateMixin implements PlayerEntityRenderStateAccessor {
    @Unique
    private NbtCompound parrotNBTLeft = null;
    @Unique
    private NbtCompound parrotNBTRight = null;

    @Override
    public void breed_Parrots_With_Seeds$setParrotNBTLeft(NbtCompound parrotNBTLeft) {
        this.parrotNBTLeft = parrotNBTLeft;
    }

    @Override
    public void breed_Parrots_With_Seeds$setParrotNBTRight(NbtCompound parrotNBTRight) {
        this.parrotNBTRight = parrotNBTRight;
    }

    @Override
    public NbtCompound breed_Parrots_With_Seeds$getParrotNBTLeft() {
        return this.parrotNBTLeft;
    }

    @Override
    public NbtCompound breed_Parrots_With_Seeds$getParrotNBTRight() {
        return this.parrotNBTRight;
    }
}