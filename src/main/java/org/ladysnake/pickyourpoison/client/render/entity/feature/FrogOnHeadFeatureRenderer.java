package org.ladysnake.pickyourpoison.client.render.entity.feature;

import org.ladysnake.pickyourpoison.client.PickYourPoisonClient;
import org.ladysnake.pickyourpoison.client.render.model.FrogOnHeadModel;
import org.ladysnake.pickyourpoison.common.item.PoisonDartFrogBowlItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;

import java.awt.*;

public class FrogOnHeadFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {
    public static FrogOnHeadModel FROG_MODEL;

    public FrogOnHeadFeatureRenderer(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context, EntityModelLoader loader) {
        super(context);
        FROG_MODEL = new FrogOnHeadModel(loader.getModelPart(FrogOnHeadModel.MODEL_LAYER));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
        if (!entity.isInvisible() && PickYourPoisonClient.FROGGY_PLAYERS_CLIENT.contains(entity.getUuid())) {
            Item item = entity.getEquippedStack(EquipmentSlot.HEAD).getItem();
            if (item instanceof PoisonDartFrogBowlItem poisonDartFrogBowlItem) {
                matrices.push();
                getContextModel().head.rotate(matrices);
                int color = new Color(1.0f, 1.0f, 1.0f, 1.0f).getRGB(); // The color
                FROG_MODEL.render(matrices, vertexConsumers.getBuffer(RenderLayer.getEntityCutout(poisonDartFrogBowlItem.texture)), light, OverlayTexture.DEFAULT_UV, color);
                matrices.pop();
            }
        }
    }
}
