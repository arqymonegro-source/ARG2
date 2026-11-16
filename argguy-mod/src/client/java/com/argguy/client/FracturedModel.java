package com.argguy.client;

import com.argguy.ArgGuyMod;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Cubes and hierarchy converted from argguy.bbmodel: body (hips pivot) > head, right_arm, left_arm; legs at the root.
 * Animation = the premade Blockbench clips (Idle / Walk / Run, see FracturedAnims)
 *           + code-driven layers on top (the ones from the Figura avatar): head tracking, breathing, nerves,
 *             hunting "mutilation", Spawn-style crawl, Lithic-style shake.
 * Every motion here is slow or small: nothing flickers or strobes.
 */
public class FracturedModel extends EntityModel<FracturedRenderState> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ArgGuyMod.id("fractured"), "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final KeyframeAnimation idleAnim;
    private final KeyframeAnimation walkAnim;
    private final KeyframeAnimation runAnim;

    public FracturedModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = body.getChild("head");
        this.rightArm = body.getChild("right_arm");
        this.leftArm = body.getChild("left_arm");
        this.rightLeg = root.getChild("right_leg");
        this.leftLeg = root.getChild("left_leg");
        this.idleAnim = FracturedAnims.IDLE.bake(root);
        this.walkAnim = FracturedAnims.WALK.bake(root);
        this.runAnim = FracturedAnims.RUN.bake(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4F, -20F, -2F, 8F, 20F, 4F)
                .texOffs(0, 40).addBox(-3.5F, -19.5F, -1.5F, 7F, 19F, 3F)
                .texOffs(24, 16).addBox(-4F, -20F, -2F, 8F, 20F, 4F, new CubeDeformation(0.25F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(32, 64).addBox(-4F, -8F, -4F, 1F, 8F, 8F)
                .texOffs(50, 69).addBox(3F, -8F, -4F, 1F, 8F, 8F)
                .texOffs(68, 21).addBox(-3F, -8F, -4F, 6F, 1F, 8F)
                .texOffs(68, 30).addBox(-3F, -1F, -4F, 6F, 1F, 8F)
                .texOffs(32, 0).addBox(-3F, -7F, 3F, 6F, 6F, 1F)
                .texOffs(32, 7).addBox(-3F, -7F, -4F, 6F, 6F, 0F)
                .texOffs(80, 39).addBox(-3.5F, -7.5F, 0.5F, 7F, 7F, 3F)
                .texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.5F)),
                PartPose.offset(0F, -20.25F, 0F));
        PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(20, 40).addBox(-4F, -1F, -2F, 4F, 20F, 4F)
                .texOffs(68, 39).addBox(-3.5F, -0.5F, -1.5F, 3F, 19F, 3F)
                .texOffs(36, 40).addBox(-4F, -1F, -2F, 4F, 20F, 4F, new CubeDeformation(0.25F)),
                PartPose.offset(-4F, -19F, 0F));
        PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(48, 0).addBox(0F, -1F, -2F, 4F, 20F, 4F)
                .texOffs(68, 61).addBox(0.5F, -0.5F, -1.5F, 3F, 19F, 3F)
                .texOffs(52, 24).addBox(0F, -1F, -2F, 4F, 20F, 4F, new CubeDeformation(0.25F)),
                PartPose.offset(4F, -19F, 0F));
        PartDefinition right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(52, 48).addBox(-2F, 0F, -2F, 4F, 17F, 4F)
                .texOffs(32, 80).addBox(-1.5F, 0.5F, -1.5F, 3F, 16F, 3F)
                .texOffs(0, 62).addBox(-2F, 0F, -2F, 4F, 17F, 4F, new CubeDeformation(0.25F)),
                PartPose.offset(-2F, 7F, 0F));
        PartDefinition left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-2F, 0F, -2F, 4F, 17F, 4F)
                .texOffs(80, 0).addBox(-1.5F, 0.5F, -1.5F, 3F, 16F, 3F)
                .texOffs(16, 64).addBox(-2F, 0F, -2F, 4F, 17F, 4F, new CubeDeformation(0.25F)),
                PartPose.offset(2F, 7F, 0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(FracturedRenderState s) {
        super.setupAnim(s);   // resets the pose

        for (ModelPart part : new ModelPart[]{body, head, rightArm, leftArm, rightLeg, leftLeg}) {
            part.xScale = 1.0F;
            part.yScale = 1.0F;
            part.zScale = 1.0F;
        }

        // 1) the premade clips ------------------------------------------------------------------
        boolean moving = s.walkAnimationSpeed > 0.03F;
        if (moving && s.hunting) {
            this.runAnim.applyWalk(s.walkAnimationPos, s.walkAnimationSpeed, 2.0F, 3.0F);
        } else if (moving) {
            this.walkAnim.applyWalk(s.walkAnimationPos, s.walkAnimationSpeed, 2.0F, 2.5F);
        } else {
            this.idleAnim.apply(s.idleAnimationState, s.ageInTicks);
        }

        float t = s.ageInTicks;

        // 2) code-driven layers (from the Figura avatar) -----------------------------------------
        // head tracking, damped so the authored head pose stays
        this.head.yRot += Mth.clamp(s.yRot * Mth.DEG_TO_RAD, -1.0F, 1.0F) * 0.5F;
        this.head.xRot += Mth.clamp(s.xRot * Mth.DEG_TO_RAD, -0.8F, 0.8F) * 0.5F;

        // breathing: arms lift, chest swells
        float breathe = Mth.sin(t * 0.09F) * 0.04F;
        this.rightArm.zRot += 0.05F + Math.abs(breathe);
        this.leftArm.zRot -= 0.05F + Math.abs(breathe);
        this.body.yScale = 1.0F + Math.abs(breathe) * 0.25F;

        // nerves: slow irregular drift while standing still
        if (!moving) {
            this.head.zRot += Mth.sin(t * 0.043F) * 0.05F;
            this.head.yRot += Mth.sin(t * 0.027F + 1.4F) * 0.07F;
            this.leftArm.xRot += Mth.sin(t * 0.11F) * 0.03F;
        }

        // Spawn: down on all fours
        if (s.crawling) {
            this.body.xRot += 0.55F;
            this.head.xRot -= 0.50F;
            this.rightArm.xRot -= 0.80F;
            this.leftArm.xRot -= 0.80F;
        }

        // Lithic: shaking just before it breaks free (small amplitude, ~half a second per cycle)
        if (s.shaking) {
            this.body.xRot += Mth.sin(t * 2.7F) * 0.05F;
            this.body.zRot += Mth.sin(t * 2.3F + 1.0F) * 0.06F;
            this.head.xRot += Mth.sin(t * 3.1F) * 0.06F;
        }

        // Umbrella Man: "model mutilation" while hunting, deliberately slow (several seconds per cycle)
        if (s.hunting) {
            float m = t * 0.045F;
            this.head.xScale = 1.0F + 0.10F * Mth.sin(m * 1.3F);
            this.head.yScale = 1.0F + 0.20F * Mth.sin(m * 0.9F + 1.0F);
            this.head.zRot += 0.28F * Mth.sin(m * 0.6F);
            this.rightArm.yScale = 1.35F + 0.25F * Mth.sin(m * 0.7F);
            this.leftArm.yScale = 1.35F + 0.25F * Mth.sin(m * 0.8F + 2.0F);
            this.rightLeg.xScale = 1.0F + 0.15F * Mth.sin(m * 1.1F);
            this.leftLeg.yScale = 1.0F + 0.12F * Mth.sin(m * 0.9F + 0.5F);
        } else {
            this.rightArm.yScale = 1.08F;   // the arms are always a little too long
            this.leftArm.yScale = 1.08F;
        }
    }
}
