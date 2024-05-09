package net.zelythia.forge.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.zelythia.AutoTools;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MultiPlayerGameMode.class)
public class BlockBreakMixin {

    @Shadow
    @Final
    private Minecraft minecraft;


    @Inject(at = @At("HEAD"), method = "startDestroyBlock")
    private void startDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        AutoTools.onBlockBreaking(minecraft, minecraft.hitResult);
    }


    @Inject(at = @At("HEAD"), method = "continueDestroyBlock")
    private void continueDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        AutoTools.onBlockBreaking(minecraft, minecraft.hitResult);
    }
}
