package com.java_unbound.loader.gecko_lib.entity.rendering;

import com.geckolib.cache.model.GeoBone;
import com.geckolib.model.GeoModel;
import com.geckolib.renderer.GeoReplacedEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;

import com.java_unbound.loader.gecko_lib.entity.EntityRenderPass;
import com.java_unbound.loader.gecko_lib.entity.GenericReplacedEntity;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;

import java.util.regex.Pattern;

public class GenericReplacedEntityPassRenderer<R extends LivingEntityRenderState & GeoRenderState> extends GeoReplacedEntityRenderer<GenericReplacedEntity, Entity, R> {

    private final EntityRenderPass Pass;

    public GenericReplacedEntityPassRenderer(
            EntityRendererProvider.Context Context,
            EntityRenderPass Pass,
            GenericReplacedEntity Animatable,
            GeoModel<GenericReplacedEntity> AnimationModel,
            float ScaleWidth,
            float ScaleHeight
    ) {
        super(Context, new BedrockGeoModel(Pass, AnimationModel), Animatable);

        this.Pass = Pass;

        withScale(ScaleWidth, ScaleHeight);
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<R> RenderPassInfo, BoneSnapshots Snapshots) {
        super.adjustModelBonesForRender(RenderPassInfo, Snapshots);

        for (EntityRenderPass.PartVisibility Part : this.Pass.PartVisibility()) {
            if (Part.BonePattern() == null || Part.BonePattern().isBlank()) {
                continue;
            }

            for (GeoBone Bone : RenderPassInfo.model().topLevelBones()) {
                ApplyVisibility(Bone, Snapshots, Part);
            }
        }
    }

    private void ApplyVisibility(
            GeoBone Bone,
            BoneSnapshots Snapshots,
            EntityRenderPass.PartVisibility Part
    ) {
        if (MatchesBonePattern(Bone.name(), Part.BonePattern())) {
            Snapshots.ifPresent(Bone.name(), Snapshot -> {
                Snapshot.skipRender(!Part.Visible());
                Snapshot.skipChildrenRender(!Part.Visible());
            });
        }

        for (GeoBone Child : Bone.children()) {
            ApplyVisibility(Child, Snapshots, Part);
        }
    }

    private static boolean MatchesBonePattern(String BoneName, String Pattern) {
        if (BoneName == null || Pattern == null) {
            return false;
        }

        StringBuilder Regex = new StringBuilder();

        for (char Character : Pattern.toCharArray()) {
            if (Character == '*') {
                Regex.append(".*");
            } else {
                Regex.append(java.util.regex.Pattern.quote(String.valueOf(Character)));
            }
        }

        return BoneName.matches(Regex.toString());
    }

    @Override
    public void postRenderPass(RenderPassInfo<R> RenderPassInfo, SubmitNodeCollector RenderTasks) {

    }
}