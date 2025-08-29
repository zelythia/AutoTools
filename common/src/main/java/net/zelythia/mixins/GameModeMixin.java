package net.zelythia.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.ControllableCompat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MultiPlayerGameMode.class)
public class GameModeMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Unique
    private HitResult autoTools$lastHit;

    @Unique
    SystemToast.SystemToastId autoTools$toastId = new SystemToast.SystemToastId(276);


    @Inject(at = @At("HEAD"), method = "ensureHasSentCarriedItem")
    private void ensureHasSentCarriedItem(CallbackInfo ci) {
        if (minecraft.hitResult.equals(this.autoTools$lastHit)) return;

        if (minecraft.options.keyAttack.isDown() || ControllableCompat.attackDown()) {
            AutoTools.onBlockBreaking(minecraft, minecraft.hitResult);
            autoTools$lastHit = minecraft.hitResult;
        }
    }

    @Inject(at = @At("HEAD"), method = "startDestroyBlock", cancellable = true)
    private void startDestroyBlock(BlockPos blockPos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (minecraft.player.getInventory().getSelectedItem().getMaxDamage() > 0 && AutoToolsConfig.DURABILITY_CHECK && !AutoTools.checkDurability(minecraft.player.getInventory().getSelectedItem())) {
            cir.setReturnValue(false);
            SystemToast.addOrUpdate(minecraft.getToastManager(), autoTools$toastId, Component.literal("AutoTools"), Component.translatable("ui.toast.autotools.durability_warning", AutoToolsConfig.MIN_DURABILITY < 1 ? AutoToolsConfig.MIN_DURABILITY * 100 + "%" : AutoToolsConfig.MIN_DURABILITY));
        }

        //FIXME Probably not needed anymore
        //Adds a 1 Tick = 50ms delay when breaking blocks to prevent desyncs like Ghost-Blocks
        if(AutoToolsConfig.TOGGLE && AutoToolsConfig.EXPERIMENTAL_BREAK_DELAY){
            if(AutoTools.swapped){
                cir.setReturnValue(false);
                AutoTools.swapped = false;
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "attack", cancellable = true)
    private void attack(CallbackInfo ci) {
        if (minecraft.player.getInventory().getSelectedItem().getMaxDamage() > 0 && AutoToolsConfig.DURABILITY_CHECK && !AutoTools.checkDurability(minecraft.player.getInventory().getSelectedItem())) {
            ci.cancel();
            SystemToast.addOrUpdate(minecraft.getToastManager(), autoTools$toastId, Component.literal("AutoTools"), Component.translatable("ui.toast.autotools.durability_warning", AutoToolsConfig.MIN_DURABILITY < 1 ? AutoToolsConfig.MIN_DURABILITY * 100 + "%" : AutoToolsConfig.MIN_DURABILITY));
        }
    }
}
