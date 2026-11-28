package com.java_unbound.mixin.resourcepacks;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.resourcepack.ResourcePackOrder;
import com.java_unbound.shared.resources.ResourceUpdater;
import com.java_unbound.shared.resources.textures.TextureUpdater;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "reloadResourcePacks", at = @At("HEAD"), require = 1)
    private void JavaUnbound$BeforePackReload(CallbackInfoReturnable<CompletableFuture<Void>> Callback) {
        Minecraft Client = (Minecraft) (Object) this;

        JavaUnbound.LOGGER.info("[MinecraftMixin] reloadResourcePacks called");

        try {
            PackRepository Repository = Client.getResourcePackRepository();

            if (Repository == null) {
                JavaUnbound.LOGGER.error("[MinecraftMixin] PackRepository is null, skipping mapping");
                return;
            }

            if (!ResourcePackOrder.ConsumeSelectionChange(Repository)) {
                JavaUnbound.LOGGER.info("[MinecraftMixin] Mapping already up to date, reloading without remap");
                return;
            }

            JavaUnbound.LOGGER.info("[MinecraftMixin] Rebuilding mapping before reload");

            ResourcePackOrder.Update(Repository);
            ResourceUpdater.UpdateResources();
            TextureUpdater.LoadTextures();

            JavaUnbound.LOGGER.info("[MinecraftMixin] Mapping ready, reload continues");
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[MinecraftMixin] Failed to rebuild mapping before reload", Exception);
        }
    }
}