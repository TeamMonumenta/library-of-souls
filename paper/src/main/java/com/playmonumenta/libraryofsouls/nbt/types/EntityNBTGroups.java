package com.playmonumenta.libraryofsouls.nbt.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Bee;
import org.bukkit.entity.ChestedHorse;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Goat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.PiglinAbstract;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Spellcaster;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Warden;
import org.bukkit.entity.Zombie;

/**
 * Static NBT field groupings per Paper entity interface, used for book display and tab completion.
 * Groups are ordered general → specific; results are cached per EntityType.
 */
public final class EntityNBTGroups {

	public record NbtField(String key, NbtFieldType type) {
	}

	public record Group(String displayName, List<NbtField> fields) {
	}

	private static final Group ENTITY = new Group("Entity", List.of(
		new NbtField("CustomName", NbtFieldType.STRING),
		new NbtField("CustomNameVisible", NbtFieldType.BOOLEAN),
		new NbtField("Silent", NbtFieldType.BOOLEAN),
		new NbtField("NoGravity", NbtFieldType.BOOLEAN),
		new NbtField("Invulnerable", NbtFieldType.BOOLEAN),
		new NbtField("Glowing", NbtFieldType.BOOLEAN),
		new NbtField("Tags", NbtFieldType.LIST),
		new NbtField("Pos", NbtFieldType.LIST),
		new NbtField("Motion", NbtFieldType.LIST),
		new NbtField("Rotation", NbtFieldType.LIST),
		new NbtField("UUID", NbtFieldType.INT_ARRAY),
		new NbtField("FallDistance", NbtFieldType.FLOAT),
		new NbtField("Fire", NbtFieldType.SHORT),
		new NbtField("Air", NbtFieldType.SHORT),
		new NbtField("OnGround", NbtFieldType.BOOLEAN),
		new NbtField("PortalCooldown", NbtFieldType.INT),
		new NbtField("WorldUUIDLeast", NbtFieldType.LONG),
		new NbtField("WorldUUIDMost", NbtFieldType.LONG)));

	private static final Group LIVING_ENTITY = new Group("LivingEntity", List.of(
		new NbtField("Health", NbtFieldType.FLOAT),
		new NbtField("AbsorptionAmount", NbtFieldType.FLOAT),
		new NbtField("DeathTime", NbtFieldType.SHORT),
		new NbtField("HurtTime", NbtFieldType.SHORT),
		new NbtField("FallFlying", NbtFieldType.BOOLEAN),
		new NbtField("active_effects", NbtFieldType.ACTIVE_EFFECTS),
		new NbtField("SleepingX", NbtFieldType.INT),
		new NbtField("SleepingY", NbtFieldType.INT),
		new NbtField("SleepingZ", NbtFieldType.INT)));

	private static final Group MOB = new Group("Mob", List.of(
		new NbtField("NoAI", NbtFieldType.BOOLEAN),
		new NbtField("CanPickUpLoot", NbtFieldType.BOOLEAN),
		new NbtField("PersistenceRequired", NbtFieldType.BOOLEAN),
		new NbtField("LeftHanded", NbtFieldType.BOOLEAN),
		new NbtField("Team", NbtFieldType.STRING),
		new NbtField("HandItems", NbtFieldType.HAND_ITEMS),
		new NbtField("ArmorItems", NbtFieldType.ARMOR_ITEMS),
		new NbtField("HandDropChances", NbtFieldType.LIST),
		new NbtField("ArmorDropChances", NbtFieldType.LIST),
		new NbtField("DeathLootTable", NbtFieldType.STRING),
		new NbtField("DeathLootTableSeed", NbtFieldType.LONG)));

	private static final Group AGEABLE = new Group("Ageable", List.of(
		new NbtField("Age", NbtFieldType.INT),
		new NbtField("AgeLocked", NbtFieldType.BOOLEAN),
		new NbtField("InLove", NbtFieldType.INT)));

	private static final Group TAMEABLE = new Group("Tameable", List.of(
		new NbtField("Owner", NbtFieldType.INT_ARRAY),
		new NbtField("Sitting", NbtFieldType.BOOLEAN)));

	private static final Group ZOMBIE = new Group("Zombie", List.of(
		new NbtField("IsBaby", NbtFieldType.BOOLEAN),
		new NbtField("CanBreakDoors", NbtFieldType.BOOLEAN),
		new NbtField("DrownedConversionTime", NbtFieldType.INT),
		new NbtField("InWaterTime", NbtFieldType.INT)));

	private static final Group CREEPER = new Group("Creeper", List.of(
		new NbtField("powered", NbtFieldType.BOOLEAN),
		new NbtField("ExplosionRadius", NbtFieldType.BYTE),
		new NbtField("Fuse", NbtFieldType.SHORT),
		new NbtField("ignited", NbtFieldType.BOOLEAN)));

	private static final Group SLIME = new Group("Slime", List.of(
		new NbtField("Size", NbtFieldType.INT)));

	private static final Group ABSTRACT_HORSE = new Group("AbstractHorse", List.of(
		new NbtField("Tame", NbtFieldType.BOOLEAN)));

	private static final Group CHESTED_HORSE = new Group("ChestedHorse", List.of(
		new NbtField("ChestedHorse", NbtFieldType.BOOLEAN)));

	private static final Group BEE = new Group("Bee", List.of(
		new NbtField("AngerTime", NbtFieldType.INT),
		new NbtField("HasNectar", NbtFieldType.BOOLEAN),
		new NbtField("HasStung", NbtFieldType.BOOLEAN),
		new NbtField("CannotEnterHiveTicks", NbtFieldType.INT),
		new NbtField("TicksSincePollination", NbtFieldType.INT)));

	private static final Group SPELLCASTER = new Group("Spellcaster", List.of(
		new NbtField("SpellTicks", NbtFieldType.INT)));

	private static final Group PIGLIN = new Group("Piglin", List.of(
		new NbtField("IsBaby", NbtFieldType.BOOLEAN),
		new NbtField("IsImmuneToZombification", NbtFieldType.BOOLEAN),
		new NbtField("TimeInOverworld", NbtFieldType.INT)));

	private static final Group WARDEN = new Group("Warden", List.of(
		new NbtField("AngerLevel", NbtFieldType.INT)));

	private static final Group GOAT = new Group("Goat", List.of(
		new NbtField("IsScreamingGoat", NbtFieldType.BOOLEAN),
		new NbtField("HasLeftHorn", NbtFieldType.BOOLEAN),
		new NbtField("HasRightHorn", NbtFieldType.BOOLEAN)));

	private static final Group PASSENGERS = new Group("Entity (general)", List.of(
		new NbtField("Passengers", NbtFieldType.PASSENGERS)));

	private static final Map<EntityType, List<Group>> CACHE = new EnumMap<>(EntityType.class);

	private EntityNBTGroups() {
	}

	public static List<Group> getGroupsForType(EntityType type) {
		return CACHE.computeIfAbsent(type, EntityNBTGroups::computeGroups);
	}

	private static List<Group> computeGroups(EntityType type) {
		Class<?> cls = type.getEntityClass();
		if (cls == null) {
			return List.of();
		}
		var groups = new ArrayList<Group>();
		if (Entity.class.isAssignableFrom(cls)) {
			groups.add(ENTITY);
			groups.add(PASSENGERS);
		}
		if (LivingEntity.class.isAssignableFrom(cls)) {
			groups.add(LIVING_ENTITY);
		}
		if (Mob.class.isAssignableFrom(cls)) {
			groups.add(MOB);
		}
		if (Ageable.class.isAssignableFrom(cls)) {
			groups.add(AGEABLE);
		}
		if (Tameable.class.isAssignableFrom(cls)) {
			groups.add(TAMEABLE);
		}
		if (Zombie.class.isAssignableFrom(cls)) {
			groups.add(ZOMBIE);
		}
		if (Creeper.class.isAssignableFrom(cls)) {
			groups.add(CREEPER);
		}
		if (Slime.class.isAssignableFrom(cls)) {
			groups.add(SLIME);
		}
		if (AbstractHorse.class.isAssignableFrom(cls)) {
			groups.add(ABSTRACT_HORSE);
		}
		if (ChestedHorse.class.isAssignableFrom(cls)) {
			groups.add(CHESTED_HORSE);
		}
		if (Bee.class.isAssignableFrom(cls)) {
			groups.add(BEE);
		}
		if (Spellcaster.class.isAssignableFrom(cls)) {
			groups.add(SPELLCASTER);
		}
		if (PiglinAbstract.class.isAssignableFrom(cls)) {
			groups.add(PIGLIN);
		}
		if (Warden.class.isAssignableFrom(cls)) {
			groups.add(WARDEN);
		}
		if (Goat.class.isAssignableFrom(cls)) {
			groups.add(GOAT);
		}
		return Collections.unmodifiableList(groups);
	}

	/** Returns a key → NbtFieldType map for all annotated fields for the given entity type. */
	public static Map<String, NbtFieldType> getFieldTypeMap(EntityType type) {
		Map<String, NbtFieldType> map = new LinkedHashMap<>();
		for (Group g : getGroupsForType(type)) {
			for (NbtField f : g.fields()) {
				map.putIfAbsent(f.key(), f.type());
			}
		}
		return Collections.unmodifiableMap(map);
	}
}
