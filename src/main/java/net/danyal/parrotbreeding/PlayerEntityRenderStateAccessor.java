package net.danyal.parrotbreeding;

import net.minecraft.nbt.NbtCompound;

public interface PlayerEntityRenderStateAccessor {
    void breed_Parrots_With_Seeds$setParrotNBTLeft(NbtCompound parrotNBTLeft);
    void breed_Parrots_With_Seeds$setParrotNBTRight(NbtCompound parrotNBTRight);

    NbtCompound breed_Parrots_With_Seeds$getParrotNBTLeft();
    NbtCompound breed_Parrots_With_Seeds$getParrotNBTRight();
}