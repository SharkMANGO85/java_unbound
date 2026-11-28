package com.java_unbound.shared.resources;

import com.java_unbound.shared.resources.types.UIResourceUpdater;

public class ResourceUpdater {
    public static void UpdateResources() {
        ResourceUtilities.ClearTextureCache();

        UIResourceUpdater.UpdateUIResources();
    }
}
