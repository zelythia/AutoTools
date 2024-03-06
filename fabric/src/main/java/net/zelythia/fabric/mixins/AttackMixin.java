package net.zelythia.fabric.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;
import net.zelythia.AutoTools;
import net.zelythia.AutoToolsConfig;
import net.zelythia.fabric.AutoToolsFabric;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class AttackMixin {

    @Shadow
    @Nullable
    public HitResult hitResult;
    @Shadow
    @Nullable
    public LocalPlayer player;

    @Inject(at = @At("HEAD"), method = "startAttack")
    private void doAttack(CallbackInfo cir) {
        if (AutoToolsConfig.TOGGLE && AutoToolsFabric.switchItem) {
            if (player.isCreative()) {
                if (!AutoToolsConfig.DISABLECREATIVE) {
                    AutoTools.getCorrectTool(hitResult, (Minecraft) (Object) this);
                }
            } else {
                AutoTools.getCorrectTool(hitResult, (Minecraft) (Object) this);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;continueDestroyBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;)Z"), method = "continueAttack")
    private void doItemUse(CallbackInfo ci) {
        if (AutoToolsConfig.TOGGLE && AutoToolsFabric.switchItem) {
            if (player.isCreative()) {
                if (!AutoToolsConfig.DISABLECREATIVE) {
                    AutoTools.getCorrectTool(hitResult, (Minecraft) (Object) this);
                }
            } else {
                AutoTools.getCorrectTool(hitResult, (Minecraft) (Object) this);
            }
        }
    }

}
