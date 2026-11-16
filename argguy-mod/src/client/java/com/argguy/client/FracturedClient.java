package com.argguy.client;

import com.argguy.ArgGuyMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;

public class FracturedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(FracturedModel.LAYER, FracturedModel::createBodyLayer);
        EntityRendererRegistry.register(ArgGuyMod.FRACTURED, FracturedRenderer::new);
    }
}
