package org.ladysnake.pickyourpoison.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.ItemDispenserBehavior;
import net.minecraft.block.dispenser.ProjectileDispenserBehavior;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.pickyourpoison.common.entity.PoisonDartEntity;
import org.ladysnake.pickyourpoison.common.entity.PoisonDartFrogEntity;
import org.ladysnake.pickyourpoison.common.item.PoisonDartFrogBowlItem;
import org.ladysnake.pickyourpoison.common.item.ThrowingDartItem;
import org.ladysnake.pickyourpoison.common.statuseffect.EmptyStatusEffect;
import org.ladysnake.pickyourpoison.common.statuseffect.NumbnessStatusEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.bernie.geckolib.GeckoLib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class PickYourPoison implements ModInitializer {
    public static final String MODID = "pickyourpoison";
    // STATUS EFFECTS
    public static final RegistryEntry<StatusEffect> VULNERABILITY = registerStatusEffect("vulnerability", new EmptyStatusEffect(StatusEffectCategory.HARMFUL, 0xFF891C));
    //    public static final ArrayList<UUID> FROGGY_PLAYERS = new ArrayList<>();
    public static final RegistryEntry<StatusEffect> COMATOSE = registerStatusEffect("comatose", new EmptyStatusEffect(StatusEffectCategory.HARMFUL, 0x35A2F3));
    public static final RegistryEntry<StatusEffect> NUMBNESS = registerStatusEffect("numbness", new NumbnessStatusEffect(StatusEffectCategory.HARMFUL, 0x62B229));
    public static final RegistryEntry<StatusEffect> TORPOR = registerStatusEffect("torpor", new EmptyStatusEffect(StatusEffectCategory.HARMFUL, 0xD8C0B8));
    public static final RegistryEntry<StatusEffect> BATRACHOTOXIN = registerStatusEffect("batrachotoxin", new EmptyStatusEffect(StatusEffectCategory.HARMFUL, 0xEAD040));
    public static final RegistryEntry<StatusEffect> STIMULATION = registerStatusEffect("stimulation", new EmptyStatusEffect(StatusEffectCategory.HARMFUL, 0xD85252).addAttributeModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, Identifier.of("91aeaa56-376b-4498-935b-2f7f68070635"), 0.2f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)); // TODO not sure about this
    public static final boolean isTrinketsLoaded = FabricLoader.getInstance().isModLoaded("trinkets");
    // ENTITIES
    public static final EntityType<PoisonDartFrogEntity> POISON_DART_FROG = FabricEntityTypeBuilder.createMob()
            .entityFactory(PoisonDartFrogEntity::new)
            .spawnGroup(SpawnGroup.CREATURE)
            .dimensions(EntityDimensions.changing(0.5F, 0.4F))
            .spawnRestriction(SpawnLocationTypes.ON_GROUND, Heightmap.Type.MOTION_BLOCKING, PoisonDartFrogEntity::canMobSpawn)
            .build();
    public static final EntityType<PoisonDartEntity> POISON_DART = FabricEntityTypeBuilder.<PoisonDartEntity>create(SpawnGroup.MISC, PoisonDartEntity::new)
            .dimensions(EntityDimensions.changing(0.5f, 0.5f))
            .trackRangeBlocks(4)
            .trackedUpdateRate(20)
            .build();
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    // ITEMS
    public static Item POISON_DART_FROG_SPAWN_EGG;
    public static PoisonDartFrogBowlItem BLUE_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem GOLDEN_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem GREEN_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem ORANGE_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem CRIMSON_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem RED_POISON_DART_FROG_BOWL;
    public static PoisonDartFrogBowlItem LUXALAMANDER_BOWL;
    public static PoisonDartFrogBowlItem RANA_BOWL;
    public static Item THROWING_DART;
    public static Item COMATOSE_POISON_DART;
    public static Item BATRACHOTOXIN_POISON_DART;
    public static Item NUMBNESS_POISON_DART;
    public static Item VULNERABILITY_POISON_DART;
    public static Item TORPOR_POISON_DART;
    public static Item STIMULATION_POISON_DART;
    public static Item BLINDNESS_POISON_DART;
    // SOUNDS
    public static final SoundEvent ENTITY_POISON_DART_FROG_AMBIENT = SoundEvent.of(id("entity.poison_dart_frog.ambient"));
    public static final SoundEvent ENTITY_POISON_DART_FROG_HURT = SoundEvent.of(id("entity.poison_dart_frog.hurt"));
    public static final SoundEvent ENTITY_POISON_DART_FROG_DEATH = SoundEvent.of(id("entity.poison_dart_frog.death"));
    public static final SoundEvent ENTITY_POISON_DART_HIT = SoundEvent.of(id("entity.poison_dart.hit"));
    public static final SoundEvent ITEM_POISON_DART_FROG_BOWL_FILL = SoundEvent.of(id("item.poison_dart_frog_bowl.fill"));
    public static final SoundEvent ITEM_POISON_DART_FROG_BOWL_EMPTY = SoundEvent.of(id("item.poison_dart_frog_bowl.empty"));
    public static final SoundEvent ITEM_POISON_DART_FROG_BOWL_LICK = SoundEvent.of(id("item.poison_dart_frog_bowl.lick"));
    public static final SoundEvent ITEM_POISON_DART_COAT = SoundEvent.of(id("item.poison_dart.coat"));
    public static final SoundEvent ITEM_POISON_DART_THROW = SoundEvent.of(id("item.poison_dart.throw"));

    public static Identifier id(String path) {
        return Identifier.of(MODID, path);
    }

    private static <T extends Entity> void registerEntity(String name, EntityType<T> entityType) {
        Registry.register(Registries.ENTITY_TYPE, id(name), entityType);
    }

    public static <T extends Item> T registerItem(String name, T item) {
        Registry.register(Registries.ITEM, id(name), item);
        return item;
    }

    public static Item registerDartItem(String name, Item item) {
        registerItem(name, item);

        DispenserBlock.registerBehavior(item, new ItemDispenserBehavior() {
            @Override
            public ItemStack dispenseSilently(BlockPointer pointer, ItemStack itemStack) {
                World world = pointer.world();
                Direction direction = pointer.state().get(DispenserBlock.FACING);
                Position position = ProjectileItem.Settings.DEFAULT.positionFunction().getDispensePosition(pointer, direction);

                PoisonDartEntity throwingDart = new PoisonDartEntity(world, position.getX(), position.getY(), position.getZ(), itemStack);
                world.spawnEntity(throwingDart);

                float uncertainty = 6.0F;
                float power = 1.1F;

                throwingDart.setVelocity(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ(), power, uncertainty);
                throwingDart.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
                throwingDart.setDamage(throwingDart.getDamage());
                throwingDart.setItem(itemStack);
                StatusEffectInstance statusEffectInstance = ((ThrowingDartItem) itemStack.getItem()).getStatusEffectInstance();
                if (statusEffectInstance != null) {
                    StatusEffectInstance potion = new StatusEffectInstance(statusEffectInstance);
                    throwingDart.addEffect(potion);
                    throwingDart.setColor(potion.getEffectType().value().getColor());
                }

                itemStack.decrement(1);
                return itemStack;
            }
        });

        return item;
    }

    private static RegistryEntry<StatusEffect> registerStatusEffect(String name, StatusEffect effect) {
        return Registry.registerReference(Registries.STATUS_EFFECT, id(name), effect);
    }

    public static PoisonDartFrogBowlItem[] getAllFrogBowls() {
        return new PoisonDartFrogBowlItem[]{
                PickYourPoison.BLUE_POISON_DART_FROG_BOWL,
                PickYourPoison.RED_POISON_DART_FROG_BOWL,
                PickYourPoison.CRIMSON_POISON_DART_FROG_BOWL,
                PickYourPoison.GREEN_POISON_DART_FROG_BOWL,
                PickYourPoison.GOLDEN_POISON_DART_FROG_BOWL,
                PickYourPoison.ORANGE_POISON_DART_FROG_BOWL,
                PickYourPoison.LUXALAMANDER_BOWL,
                PickYourPoison.RANA_BOWL
        };
    }

    public static boolean isComatose(@Nullable LivingEntity entity) {
        return entity != null &&
                entity.hasStatusEffect(PickYourPoison.COMATOSE) &&
                !entity.isSpectator() &&
                !(entity instanceof PlayerEntity player && player.isCreative());
    }

    // INIT
    @Override
    public void onInitialize() {
        // FROGGY COSMETICS
//        ServerLifecycleEvents.SERVER_STARTING.register(server -> new FroggyPlayerListLoaderThread().start());
//        ServerLifecycleEvents.SERVER_STOPPING.register(server -> FROGGY_PLAYERS.clear());

        // ENTITIES
        registerEntity("poison_dart_frog", POISON_DART_FROG);
        FabricDefaultAttributeRegistry.register(POISON_DART_FROG, PoisonDartFrogEntity.createPoisonDartFrogAttributes());
        BiomeModifications.addSpawn(
                biome -> biome.getBiomeRegistryEntry().isIn(BiomeTags.IS_JUNGLE),
                SpawnGroup.CREATURE, POISON_DART_FROG, 50, 2, 5
        );
        registerEntity("poison_dart", POISON_DART);

        // ITEMS
        POISON_DART_FROG_SPAWN_EGG = registerItem("poison_dart_frog_spawn_egg", new SpawnEggItem(POISON_DART_FROG, 0x5BBCF4, 0x22286B, (new Item.Settings())));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register((entries) -> entries.add(POISON_DART_FROG_SPAWN_EGG));

        BLUE_POISON_DART_FROG_BOWL = registerItem("blue_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/blue.png")));
        GOLDEN_POISON_DART_FROG_BOWL = registerItem("golden_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/golden.png")));
        GREEN_POISON_DART_FROG_BOWL = registerItem("green_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/green.png")));
        ORANGE_POISON_DART_FROG_BOWL = registerItem("orange_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/orange.png")));
        CRIMSON_POISON_DART_FROG_BOWL = registerItem("crimson_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/crimson.png")));
        RED_POISON_DART_FROG_BOWL = registerItem("red_poison_dart_frog_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).food(foodComponent()), id("textures/entity/red.png")));
        LUXALAMANDER_BOWL = registerItem("luxalamander_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).rarity(Rarity.RARE).food(foodComponent()), id("textures/entity/luxintrus.png")));
        RANA_BOWL = registerItem("rana_bowl", new PoisonDartFrogBowlItem((new Item.Settings()).maxCount(1).rarity(Rarity.RARE).food(foodComponent()), id("textures/entity/rana.png")));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register((entries) -> entries.addAll(Stream.of(
                BLUE_POISON_DART_FROG_BOWL,
                GOLDEN_POISON_DART_FROG_BOWL,
                GREEN_POISON_DART_FROG_BOWL,
                ORANGE_POISON_DART_FROG_BOWL,
                CRIMSON_POISON_DART_FROG_BOWL,
                RED_POISON_DART_FROG_BOWL,
                LUXALAMANDER_BOWL,
                RANA_BOWL
        ).map(Item::getDefaultStack).toList()));
        THROWING_DART = registerDartItem("throwing_dart", new ThrowingDartItem((new Item.Settings()).maxCount(64), null));
        COMATOSE_POISON_DART = registerDartItem("comatose_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.COMATOSE), 100))); // 5s
        BATRACHOTOXIN_POISON_DART = registerDartItem("batrachotoxin_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.BATRACHOTOXIN), 80))); // 4s
        NUMBNESS_POISON_DART = registerDartItem("numbness_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.NUMBNESS), 200))); // 10s
        VULNERABILITY_POISON_DART = registerDartItem("vulnerability_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.VULNERABILITY), 200))); // 10s
        TORPOR_POISON_DART = registerDartItem("torpor_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.TORPOR), 200))); // 10s
        STIMULATION_POISON_DART = registerDartItem("stimulation_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance((PickYourPoison.STIMULATION), 600))); // 30s
        BLINDNESS_POISON_DART = registerDartItem("blindness_poison_dart", new ThrowingDartItem((new Item.Settings()).maxCount(1), new StatusEffectInstance(StatusEffects.BLINDNESS, 200))); // 10s
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register((entries) -> entries.addAll(Stream.of(
                THROWING_DART,
                COMATOSE_POISON_DART,
                BATRACHOTOXIN_POISON_DART,
                NUMBNESS_POISON_DART,
                VULNERABILITY_POISON_DART,
                TORPOR_POISON_DART,
                STIMULATION_POISON_DART,
                BLINDNESS_POISON_DART
        ).map(Item::getDefaultStack).toList()));

        // SOUNDS
        Registry.register(Registries.SOUND_EVENT, ENTITY_POISON_DART_FROG_AMBIENT.getId(), ENTITY_POISON_DART_FROG_AMBIENT);
        Registry.register(Registries.SOUND_EVENT, ENTITY_POISON_DART_FROG_HURT.getId(), ENTITY_POISON_DART_FROG_HURT);
        Registry.register(Registries.SOUND_EVENT, ENTITY_POISON_DART_FROG_DEATH.getId(), ENTITY_POISON_DART_FROG_DEATH);
        Registry.register(Registries.SOUND_EVENT, ENTITY_POISON_DART_HIT.getId(), ENTITY_POISON_DART_HIT);
        Registry.register(Registries.SOUND_EVENT, ITEM_POISON_DART_FROG_BOWL_FILL.getId(), ITEM_POISON_DART_FROG_BOWL_FILL);
        Registry.register(Registries.SOUND_EVENT, ITEM_POISON_DART_FROG_BOWL_EMPTY.getId(), ITEM_POISON_DART_FROG_BOWL_EMPTY);
        Registry.register(Registries.SOUND_EVENT, ITEM_POISON_DART_FROG_BOWL_LICK.getId(), ITEM_POISON_DART_FROG_BOWL_LICK);
        Registry.register(Registries.SOUND_EVENT, ITEM_POISON_DART_COAT.getId(), ITEM_POISON_DART_COAT);
        Registry.register(Registries.SOUND_EVENT, ITEM_POISON_DART_THROW.getId(), ITEM_POISON_DART_THROW);

        // TICK
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            for (ServerPlayerEntity player : world.getPlayers()) {
                if (player.hasStatusEffect((TORPOR)) && (player.age % (100 / (MathHelper.clamp(player.getStatusEffect((TORPOR)).getAmplifier() + 1, 1, 20))) == 0)) {
                    player.getHungerManager().add(1, 0);
                }
            }
        });
    }

    private static FoodComponent foodComponent() {
        return new FoodComponent.Builder().alwaysEdible().build();
    }

//    private static class FroggyPlayerListLoaderThread extends Thread {
//        public FroggyPlayerListLoaderThread() {
//            setName("Pick Your Poison Equippable Frogs Thread");
//            setDaemon(true);
//        }
//
//        @Override
//        public void run() {
//            try (BufferedInputStream stream = IOUtils.buffer(new URL(FROGGY_PLAYERS_URL).openStream())) {
//                Properties properties = new Properties();
//                properties.load(stream);
//                synchronized (FROGGY_PLAYERS) {
//                    FROGGY_PLAYERS.clear();
//                    for (Object o : JsonReader.readJsonFromUrl(FROGGY_PLAYERS_URL).toList()) {
//                        FROGGY_PLAYERS.add(UUID.fromString((String) o));
//                    }

    /// /                    System.out.println(FROGGY_PLAYERS);
//                }
//            } catch (IOException e) {
//                LOGGER.error("Failed to load froggy list.");
//            }
//        }
//    }

    public static class JsonReader {

        public static final Gson GSON = new GsonBuilder().create();

        public static List<UUID> readJsonFromUrl(String url) throws IOException {
            try (InputStream is = new URL(url).openStream()) {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                return GSON.fromJson(rd, new TypeToken<>() {
                });
            }
        }
    }
}
