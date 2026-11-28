package com.java_unbound.mixin.resourcepacks;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resourcepack.ResourcePackOrder;
import com.java_unbound.shared.resources.ResourceUpdater;
import com.java_unbound.shared.resources.textures.TextureUpdater;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public abstract class OptionsMixin {
    @Inject(method = "loadSelectedResourcePacks", at = @At("RETURN"), require = 0)
    private void JavaUnbound$AfterLoadSelectedPacks(PackRepository Repository, CallbackInfo Callback) {
        JavaUnbound.LOGGER.info("[OptionsMixin] Selected resource packs loaded from options");

        try {
            if (!ResourcePackOrder.ConsumeSelectionChange(Repository)) {
                JavaUnbound.LOGGER.info("[OptionsMixin] Mapping already up to date");
                return;
            }

            ResourcePackOrder.Update(Repository);
            ResourceUpdater.UpdateResources();
            TextureUpdater.LoadTextures();

            JavaUnbound.LOGGER.info("[OptionsMixin] Mapping ready before the initial reload");
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[OptionsMixin] Failed to build initial mapping", Exception);
        }
    }
}