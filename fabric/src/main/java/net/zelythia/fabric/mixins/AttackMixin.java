package net.zelythia.fabric.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.HitResult;
import net.zelythia.AutoTools;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class AttackMixin {

    @Shadow
    @Nullable
    public HitResult hitResult;

    @Inject(at = @At("HEAD"), method = "startAttack")
    private void doAttack(CallbackInfo cir) {
        AutoTools.onBlockBreaking((Minecraft) (Object) this, hitResult);
    }
}
