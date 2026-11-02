package com.java_unbound.entities.geckolib.rendering;

import com.geckolib.constant.dataticket.DataTicket;

public final class BedrockRenderData {
    private BedrockRenderData() {}

    public static final DataTicket<BedrockRenderPassData> ActiveRenderPasses = DataTicket.create("java_unbound:active_render_passes", BedrockRenderPassData.class);
}