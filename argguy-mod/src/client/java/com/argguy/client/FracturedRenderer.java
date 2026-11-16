package com.argguy.client;

import com.argguy.ArgGuyMod;
import com.argguy.FracturedEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;

public class FracturedRenderer extends MobRenderer<FracturedEntity, FracturedRenderState, FracturedModel> {
    private static final Identifier TEXTURE = ArgGuyMod.id("textures/entity/fractured.png");

    public FracturedRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new FracturedModel(ctx.bakeLayer(FracturedModel.LAYER)), 0.6F);
    }

    @Override
    public FracturedRenderState createRenderState() {
        return new FracturedRenderState();
    }

    @Override
    public void extractRenderState(FracturedEntity entity, FracturedRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        int f = entity.getFlags();
        state.hunting = (f & FracturedEntity.F_HUNT) != 0;
        state.shaking = (f & FracturedEntity.F_SHAKE) != 0;
        state.crawling = (f & FracturedEntity.F_CRAWL) != 0;
        state.idleAnimationState.copyFrom(entity.idleAnimationState);
    }

    @Override
    public Identifier getTextureLocation(FracturedRenderState state) {
        return TEXTURE;
    }
}
