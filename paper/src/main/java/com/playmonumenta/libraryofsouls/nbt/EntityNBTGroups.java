package com.playmonumenta.libraryofsouls.nbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
 * Static NBT key groupings per Paper entity interface, used for book display and tab completion.
 * Groups are ordered general → specific; results are cached per EntityType.
 */
public final class EntityNBTGroups {

	public record Group(String displayName, List<String> nbtKeys) {
	}

	private static final Group ENTITY = new Group("Entity", List.of(
		"CustomName", "CustomNameVisible", "Silent", "NoGravity", "Invulnerable", "Glowing", "Tags",
		"Pos", "Motion", "Rotation", "UUID", "FallDistance", "Fire", "Air", "OnGround",
		"PortalCooldown", "WorldUUIDLeast", "WorldUUIDMost"));
	private static final Group LIVING_ENTITY = new Group("LivingEntity", List.of(
		"Health", "AbsorptionAmount", "DeathTime", "HurtTime", "FallFlying",
		"active_effects", "SleepingX", "SleepingY", "SleepingZ"));
	private static final Group MOB = new Group("Mob", List.of(
		"NoAI", "CanPickUpLoot", "PersistenceRequired", "LeftHanded", "Team",
		"HandItems", "ArmorItems", "HandDropChances", "ArmorDropChances",
		"DeathLootTable", "DeathLootTableSeed"));
	private static final Group AGEABLE = new Group("Ageable", List.of("Age", "AgeLocked", "InLove"));
	private static final Group TAMEABLE = new Group("Tameable", List.of("Owner", "Sitting"));
	private static final Group ZOMBIE = new Group("Zombie", List.of(
		"IsBaby", "CanBreakDoors", "DrownedConversionTime", "InWaterTime"));
	private static final Group CREEPER = new Group("Creeper", List.of(
		"powered", "ExplosionRadius", "Fuse", "ignited"));
	private static final Group SLIME = new Group("Slime", List.of("Size"));
	private static final Group ABSTRACT_HORSE = new Group("AbstractHorse", List.of("Tame"));
	private static final Group CHESTED_HORSE = new Group("ChestedHorse", List.of("ChestedHorse"));
	private static final Group BEE = new Group("Bee", List.of(
		"AngerTime", "HasNectar", "HasStung", "CannotEnterHiveTicks", "TicksSincePollination"));
	private static final Group SPELLCASTER = new Group("Spellcaster", List.of("SpellTicks"));
	private static final Group PIGLIN = new Group("Piglin", List.of(
		"IsBaby", "IsImmuneToZombification", "TimeInOverworld"));
	private static final Group WARDEN = new Group("Warden", List.of("AngerLevel"));
	private static final Group GOAT = new Group("Goat", List.of(
		"IsScreamingGoat", "HasLeftHorn", "HasRightHorn"));

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

	public static Set<String> getAllKeysForType(EntityType type) {
		return getGroupsForType(type).stream()
			.flatMap(g -> g.nbtKeys().stream())
			.collect(Collectors.toSet());
	}
}
