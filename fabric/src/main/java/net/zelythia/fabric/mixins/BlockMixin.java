package net.zelythia.fabric.mixins;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.zelythia.fabric.events.ClientBlockBreakEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(at = @At(value = "HEAD"), method = "destroy")
    private void blockBreakEvent(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
        ClientBlockBreakEvent.EVENT.invoker().clientBlockBreak(levelAccessor, blockPos, blockState);
    }
}