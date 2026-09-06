package com.java_unbound.mixin.resourcepack;

import com.java_unbound.JavaUnbound;
import com.java_unbound.loader.resourcepack.Folder;
import com.java_unbound.loader.resourcepack.PackLoader;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void JavaUnbound$AddPack(CallbackInfo Callback) {
        Set<RepositorySource> Sources = new LinkedHashSet<>(this.sources);

        Sources.add(Consumer -> {
            try {
                Folder.EnsureExists();

                Path ResourcePack = Folder.GetResourceFolder();
                Pack Pack = PackLoader.Create(ResourcePack);

                Consumer.accept(Pack);
            } catch (Exception Exception) {
                Exception.printStackTrace();
            }
        });

        this.sources = Sources;
    }

    @Inject(method = "reload", at = @At("TAIL"))
    private void JavaUnbound$AfterReload(CallbackInfo Callback) {
        PackRepository Repository = (PackRepository)(Object)this;

        JavaUnbound.LOGGER.warn("[Java Unbound] ===== PACK RELOAD =====");
        JavaUnbound.LOGGER.warn("[Java Unbound] Available: " + Repository.getAvailableIds());
        JavaUnbound.LOGGER.warn("[Java Unbound] Selected: " + Repository.getSelectedIds());
        JavaUnbound.LOGGER.warn("[Java Unbound] Java Unbound available: " + Repository.isAvailable("java_unbound"));
    }
}