package org.ladysnake.pickyourpoison.cca;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistryV3;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;

public final class PickYourPoisonEntityComponents implements EntityComponentInitializer {
    public static final ComponentKey<NumbnessRetributionComponent> NUMBNESS_DAMAGE =
            ComponentRegistryV3.INSTANCE.getOrCreate(Identifier.of("pickyourpoison", "numbnessdamage"), NumbnessRetributionComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerFor(LivingEntity.class, NUMBNESS_DAMAGE, world -> new NumbnessRetributionComponent());
    }
}