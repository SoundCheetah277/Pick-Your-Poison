package org.ladysnake.pickyourpoison.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.ladysnake.pickyourpoison.client.render.entity.PoisonDartEntityRenderer;
import org.ladysnake.pickyourpoison.client.render.entity.PoisonDartFrogEntityRenderer;
import org.ladysnake.pickyourpoison.client.render.model.FrogOnHeadModel;
import org.ladysnake.pickyourpoison.common.PickYourPoison;
import org.ladysnake.satin.api.event.ShaderEffectRenderCallback;
import org.ladysnake.satin.api.managed.ManagedShaderEffect;
import org.ladysnake.satin.api.managed.ShaderEffectManager;

import java.util.ArrayList;
import java.util.UUID;

public class PickYourPoisonClient implements ClientModInitializer {
    // Everyone is now considered froggy, so this list is no longer used
    public static final ArrayList<UUID> FROGGY_PLAYERS_CLIENT = new ArrayList<>();
    private static final ManagedShaderEffect BLACK_SCREEN = ShaderEffectManager.getInstance()
            .manage(Identifier.of("pickyourpoison", "shaders/post/blackscreen.json"));

    @Override
    public void onInitializeClient() {
        // MODEL LAYERS
        EntityRendererRegistry.register(PickYourPoison.POISON_DART_FROG, PoisonDartFrogEntityRenderer::new);
        EntityRendererRegistry.register(PickYourPoison.POISON_DART, PoisonDartEntityRenderer::new);
        EntityModelLayerRegistry.registerModelLayer(FrogOnHeadModel.MODEL_LAYER, FrogOnHeadModel::getTexturedModelData);

        // COMA SHADER
        ShaderEffectRenderCallback.EVENT.register(tickDelta -> {
            if (PickYourPoison.isComatose(MinecraftClient.getInstance().player)) {
                BLACK_SCREEN.render(tickDelta);
            }
        });

        // TRINKETS COMPAT
        if (PickYourPoison.isTrinketsLoaded) {
            TrinketsCompat.registerFrogTrinketRenderers(PickYourPoison.getAllFrogBowls());
        }
    }
}
