package com.java_unbound.mixin.resourcepack;

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

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private java.util.Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void JavaUnbound$AddPack(CallbackInfo Callback) {
        boolean HasClientSource = this.sources.stream().anyMatch(Source -> Source instanceof ClientPackSource);

        if (!HasClientSource) {
            return;
        }

        LinkedHashSet<RepositorySource> Sources = new LinkedHashSet<>(this.sources);

        Sources.add(Consumer -> {
            try {
                Folder.EnsureExists();

                Path ResourcePack = Folder.GetResourceFolder();
                Pack Pack = PackLoader.Create(ResourcePack);

                if (Pack != null) {
                    Consumer.accept(Pack);
                }
            } catch (IOException E) {
                Consumer.accept(null);
            }
        });

        this.sources = Sources;
    }
}