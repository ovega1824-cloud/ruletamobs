package com.mobsruleta;

import java.util.LinkedHashMap;
import java.util.Map;

import com.mobsruleta.entity.AirDragonEntity;
import com.mobsruleta.entity.DementiaEyeEntity;
import com.mobsruleta.entity.GrassCreeperEntity;
import com.mobsruleta.entity.LavaCreeperEntity;
import com.mobsruleta.entity.MindGhostEntity;
import com.mobsruleta.entity.MudGolemEntity;
import com.mobsruleta.entity.SnowCreeperEntity;
import com.mobsruleta.entity.SpringCreeperEntity;
import com.mobsruleta.entity.StalkerEntity;
import com.mobsruleta.entity.ToxicCreeperEntity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Registro de los 10 mobs y de sus huevos de aparición. */
public final class ModEntities {

    public static final Map<String, EntityType<? extends HostileEntity>> TYPES = new LinkedHashMap<>();
    public static final Map<String, Item> EGGS = new LinkedHashMap<>();

    private ModEntities() {
    }

    public static EntityType<? extends HostileEntity> tipo(String id) {
        return TYPES.get(id);
    }

    private static <T extends HostileEntity> void reg(String id, EntityType.EntityFactory<T> factory,
                                                      float ancho, float alto,
                                                      DefaultAttributeContainer.Builder atributos,
                                                      boolean inmuneAlFuego) {
        EntityType.Builder<T> builder = EntityType.Builder.create(factory, SpawnGroup.MONSTER)
                .dimensions(ancho, alto)
                .maxTrackingRange(10);
        if (inmuneAlFuego) {
            builder.makeFireImmune();
        }
        EntityType<T> tipo = Registry.register(Registries.ENTITY_TYPE, Identifier.of(MobsRuletaMod.MOD_ID, id), builder.build(id));
        FabricDefaultAttributeRegistry.register(tipo, atributos);
        TYPES.put(id, tipo);

        Mobs.Def def = Mobs.obtener(Mobs.numero(id));
        Item huevo = Registry.register(Registries.ITEM, Identifier.of(MobsRuletaMod.MOD_ID, id + "_spawn_egg"),
                new SpawnEggItem(tipo, def.egg1(), def.egg2(), new Item.Settings()));
        EGGS.put(id, huevo);
    }

    public static void registrar() {
        reg("snow_creeper", SnowCreeperEntity::new, 0.6f, 1.7f, SnowCreeperEntity.atributos(), false);
        reg("grass_creeper", GrassCreeperEntity::new, 0.7f, 1.3f, GrassCreeperEntity.atributos(), false);
        reg("spring_creeper", SpringCreeperEntity::new, 0.6f, 1.7f, SpringCreeperEntity.atributos(), false);
        reg("mind_ghost", MindGhostEntity::new, 0.9f, 1.6f, MindGhostEntity.atributos(), true);
        reg("air_dragon", AirDragonEntity::new, 1.6f, 1.0f, AirDragonEntity.atributos(), true);
        reg("dementia_eye", DementiaEyeEntity::new, 0.6f, 0.6f, DementiaEyeEntity.atributos(), true);
        reg("lava_creeper", LavaCreeperEntity::new, 0.6f, 1.7f, LavaCreeperEntity.atributos(), true);
        reg("toxic_creeper", ToxicCreeperEntity::new, 0.6f, 1.7f, ToxicCreeperEntity.atributos(), false);
        reg("stalker", StalkerEntity::new, 0.6f, 2.4f, StalkerEntity.atributos(), false);
        reg("mud_golem", MudGolemEntity::new, 1.1f, 1.8f, MudGolemEntity.atributos(), false);
    }
}
