package net.danyal.parrotbreeding;

import net.minecraft.nbt.NbtCompound;

// SINCE PARROT SHOULDER VARIANTS ARE STORED IN THE PLAYER ENTITY RENDER STATE, THE PARROTS NBT COMPOUND (USED TO DETRERMINE AGE) WILL ALSO BE STORED
// THIS IS DONE USING AN INTERFACE SO THAT THESE NEW METHODS AND NBT COMPOUNDS ARE ACCESSIBLE THROUGHOUT OTHER MIXINS
public interface PlayerEntityRenderStateAccessor {
    void breed_Parrots_With_Seeds$setParrotNBTLeft(NbtCompound parrotNBTLeft);
    void breed_Parrots_With_Seeds$setParrotNBTRight(NbtCompound parrotNBTRight);

    NbtCompound breed_Parrots_With_Seeds$getParrotNBTLeft();
    NbtCompound breed_Parrots_With_Seeds$getParrotNBTRight();
}