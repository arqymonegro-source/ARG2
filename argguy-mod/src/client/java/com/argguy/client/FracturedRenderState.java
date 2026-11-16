package com.argguy.client;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class FracturedRenderState extends LivingEntityRenderState {
    /** Hunting: the model is "mutilated" (slowly stretched / warped). */
    public boolean hunting;
    /** Lithic: about to break free, the body shakes. */
    public boolean shaking;
    /** Spawn: goes down on all fours while charging or lying in ambush. */
    public boolean crawling;
    public final AnimationState idleAnimationState = new AnimationState();
}
