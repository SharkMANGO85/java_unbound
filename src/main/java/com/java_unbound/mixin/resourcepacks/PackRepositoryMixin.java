package com.java_unbound.mixin.resourcepacks;

import com.java_unbound.JavaUnbound;
import com.java_unbound.shared.definitions.resource_pack.ResourcePackDefinition;
import com.java_unbound.shared.loader.resourcepacks.PackLoader;
import com.java_unbound.shared.loader.resourcepacks.ResourcePackLoader;
import com.java_unbound.shared.resourcepack.ResourcePackOrder;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
    @Shadow
    @Final
    @Mutable
    private Set<RepositorySource> sources;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void JavaUnbound$AddPacks(CallbackInfo Callback) {
        JavaUnbound.LOGGER.info("[PackRepositoryMixin] Adding Java Unbound repository source");

        Set<RepositorySource> Sources = new LinkedHashSet<>(this.sources);

        Sources.add(Consumer -> {
            int Count = 0;

            for (ResourcePackDefinition Definition : ResourcePackLoader.Refresh()) {
                try {
                    Consumer.accept(PackLoader.Create(Definition));
                    Count++;
                } catch (Exception Exception) {
                    JavaUnbound.LOGGER.error("[PackRepositoryMixin] Failed to create pack " + Definition.FolderName, Exception);
                }
            }

            JavaUnbound.LOGGER.info("[PackRepositoryMixin] Offered {} bedrock packs to the repository", Count);
        });

        this.sources = Sources;
    }

    @Inject(method = "reload", at = @At("TAIL"))
    private void JavaUnbound$AfterReload(CallbackInfo Callback) {
        PackRepository Repository = (PackRepository) (Object) this;

        JavaUnbound.LOGGER.info("[PackRepositoryMixin] Repository reloaded, refreshing cached positions");

        try {
            ResourcePackOrder.Update(Repository);
        } catch (Exception Exception) {
            JavaUnbound.LOGGER.error("[PackRepositoryMixin] Failed to update pack order", Exception);
        }
    }
}