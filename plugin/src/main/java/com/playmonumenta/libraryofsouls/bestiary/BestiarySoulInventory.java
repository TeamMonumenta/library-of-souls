package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.MobNBT;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeContainer;
import com.goncalomb.bukkit.nbteditor.nbt.attributes.AttributeType;
import com.goncalomb.bukkit.nbteditor.nbt.variables.EffectsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.NBTVariable;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.ChatColor;

public class BestiarySoulInventory extends CustomInventory {
	private static EnumMap<Material, Double> mDefaultArmor = new EnumMap<>(Material.class);
	private static EnumMap<EntityType, Double> mDefaultDamage = new EnumMap<>(EntityType.class);
	private static EnumMap<EntityType, Double> mDefaultSpeed = new EnumMap<>(EntityType.class);
	private static EnumMap<Material, Double> mDefaultItemDamage = new EnumMap<>(Material.class);
	private static EnumMap<EntityType, Double> mDefaultHealth = new EnumMap<>(EntityType.class);
	private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_NUMBER;
	private static final AttributeModifier.Operation SCALAR = AttributeModifier.Operation.ADD_SCALAR;

	enum DamageType {
		MELEE,
		RANGED,
		GHAST,
		CREEPER,
		CROSSBOW,
		TRIDENT,
		EVOKER,
		BLAZE
	}

	static {
		mDefaultArmor.put(Material.LEATHER_HELMET, 1.0);
		mDefaultArmor.put(Material.LEATHER_CHESTPLATE, 3.0);
		mDefaultArmor.put(Material.LEATHER_LEGGINGS, 2.0);
		mDefaultArmor.put(Material.LEATHER_BOOTS, 1.0);
		mDefaultArmor.put(Material.GOLDEN_HELMET, 2.0);
		mDefaultArmor.put(Material.GOLDEN_CHESTPLATE, 5.0);
		mDefaultArmor.put(Material.GOLDEN_LEGGINGS, 3.0);
		mDefaultArmor.put(Material.GOLDEN_BOOTS, 1.0);
		mDefaultArmor.put(Material.CHAINMAIL_HELMET, 2.0);
		mDefaultArmor.put(Material.CHAINMAIL_CHESTPLATE, 5.0);
		mDefaultArmor.put(Material.CHAINMAIL_LEGGINGS, 4.0);
		mDefaultArmor.put(Material.CHAINMAIL_BOOTS, 1.0);
		mDefaultArmor.put(Material.IRON_HELMET, 2.0);
		mDefaultArmor.put(Material.IRON_CHESTPLATE, 6.0);
		mDefaultArmor.put(Material.IRON_LEGGINGS, 5.0);
		mDefaultArmor.put(Material.IRON_BOOTS, 2.0);
		mDefaultArmor.put(Material.DIAMOND_HELMET, 3.0);
		mDefaultArmor.put(Material.DIAMOND_CHESTPLATE, 8.0);
		mDefaultArmor.put(Material.DIAMOND_LEGGINGS, 6.0);
		mDefaultArmor.put(Material.DIAMOND_BOOTS, 3.0);
		mDefaultArmor.put(Material.TURTLE_HELMET, 2.0);

		mDefaultDamage.put(EntityType.BLAZE, 6.0);
		mDefaultDamage.put(EntityType.CAVE_SPIDER, 2.0);
		mDefaultDamage.put(EntityType.CREEPER, 49.0);
		mDefaultDamage.put(EntityType.DOLPHIN, 3.0);
		//??
		mDefaultDamage.put(EntityType.DROWNED, 3.0);
		mDefaultDamage.put(EntityType.ELDER_GUARDIAN, 8.0);
		mDefaultDamage.put(EntityType.ENDERMAN, 7.0);
		mDefaultDamage.put(EntityType.ENDERMITE, 2.0);
		mDefaultDamage.put(EntityType.EVOKER, 6.0);
		mDefaultDamage.put(EntityType.GHAST, 23.0);
		mDefaultDamage.put(EntityType.GUARDIAN, 6.0);
		mDefaultDamage.put(EntityType.HUSK, 3.0);
		//??
		mDefaultDamage.put(EntityType.ILLUSIONER, 4.0);
		mDefaultDamage.put(EntityType.IRON_GOLEM, 21.0);
		mDefaultDamage.put(EntityType.MAGMA_CUBE, 6.0);
		mDefaultDamage.put(EntityType.PHANTOM, 6.0);
		mDefaultDamage.put(EntityType.ZOMBIFIED_PIGLIN, 4.0);
		//??
		mDefaultDamage.put(EntityType.PILLAGER, 4.0);
		mDefaultDamage.put(EntityType.POLAR_BEAR, 6.0);
		mDefaultDamage.put(EntityType.RAVAGER, 12.0);
		mDefaultDamage.put(EntityType.SHULKER, 4.0);
		mDefaultDamage.put(EntityType.SILVERFISH, 1.0);
		//??
		mDefaultDamage.put(EntityType.SKELETON, 2.5);
		mDefaultDamage.put(EntityType.SLIME, 2.0);
		mDefaultDamage.put(EntityType.SPIDER, 2.0);
		//??
		mDefaultDamage.put(EntityType.STRAY, 2.0);
		mDefaultDamage.put(EntityType.VEX, 3.0);
		mDefaultDamage.put(EntityType.VINDICATOR, 5.0);
		mDefaultDamage.put(EntityType.WITHER_SKELETON, 3.0);
		//??
		mDefaultDamage.put(EntityType.WITHER, 8.0);
		mDefaultDamage.put(EntityType.WOLF, 2.0);
		mDefaultDamage.put(EntityType.ZOMBIE, 3.0);
		mDefaultDamage.put(EntityType.ZOMBIE_VILLAGER, 3.0);
		//I'll just assume it works the same for each mob-it should really only be on select zombies anyway
		mDefaultItemDamage.put(Material.WOODEN_SWORD, 4.0);
		mDefaultItemDamage.put(Material.GOLDEN_SWORD, 4.0);
		mDefaultItemDamage.put(Material.STONE_SWORD, 5.0);
		mDefaultItemDamage.put(Material.IRON_SWORD, 6.0);
		mDefaultItemDamage.put(Material.DIAMOND_SWORD, 7.0);

		mDefaultSpeed.put(EntityType.SNOWMAN, 0.2);
		mDefaultSpeed.put(EntityType.BLAZE, 0.23);
		mDefaultSpeed.put(EntityType.DROWNED, 0.23);
		mDefaultSpeed.put(EntityType.HUSK, 0.23);
		mDefaultSpeed.put(EntityType.ZOMBIE, 0.23);
		mDefaultSpeed.put(EntityType.ZOMBIE_VILLAGER, 0.23);
		mDefaultSpeed.put(EntityType.ZOMBIFIED_PIGLIN, 0.23);
		mDefaultSpeed.put(EntityType.CREEPER, 0.25);
		mDefaultSpeed.put(EntityType.ENDERMITE, 0.25);
		mDefaultSpeed.put(EntityType.IRON_GOLEM, 0.25);
		mDefaultSpeed.put(EntityType.POLAR_BEAR, 0.25);
		mDefaultSpeed.put(EntityType.SILVERFISH, 0.25);
		mDefaultSpeed.put(EntityType.SKELETON, 0.25);
		mDefaultSpeed.put(EntityType.STRAY, 0.25);
		mDefaultSpeed.put(EntityType.WITCH, 0.25);
		mDefaultSpeed.put(EntityType.WITHER_SKELETON, 0.25);
		mDefaultSpeed.put(EntityType.CAT, 0.3);
		mDefaultSpeed.put(EntityType.CAVE_SPIDER, 0.3);
		mDefaultSpeed.put(EntityType.ELDER_GUARDIAN, 0.3);
		mDefaultSpeed.put(EntityType.ENDERMAN, 0.3);
		mDefaultSpeed.put(EntityType.FOX, 0.3);
		mDefaultSpeed.put(EntityType.OCELOT, 0.3);
		mDefaultSpeed.put(EntityType.RAVAGER, 0.3);
		mDefaultSpeed.put(EntityType.SPIDER, 0.3);
		mDefaultSpeed.put(EntityType.WOLF, 0.3);
		mDefaultSpeed.put(EntityType.PILLAGER, 0.35);
		mDefaultSpeed.put(EntityType.VINDICATOR, 0.35);
		mDefaultSpeed.put(EntityType.EVOKER, 0.5);
		mDefaultSpeed.put(EntityType.GUARDIAN, 0.5);
		mDefaultSpeed.put(EntityType.ILLUSIONER, 0.5);
		mDefaultSpeed.put(EntityType.WITHER, 0.6);
		mDefaultSpeed.put(EntityType.GHAST, 0.7);
		mDefaultSpeed.put(EntityType.PUFFERFISH, 0.7);
		mDefaultSpeed.put(EntityType.SHULKER, 0.0);
		mDefaultSpeed.put(EntityType.DOLPHIN, 1.2);
		mDefaultSpeed.put(EntityType.SQUID, 0.7);
		mDefaultSpeed.put(EntityType.VEX, 0.7);


	}

	private static String formatWell(String in) {
		in = in.replaceAll("\"", "");
		String sub = "";
		if (in.contains("_")) {
			String[] cuts = in.split("_");
			for (String cut : cuts) {
				if (cut.length() == 0) {
					continue;
				}
				String subCut = cut.substring(0, 1);
				subCut = subCut.toUpperCase();
				subCut += cut.substring(1);
				sub += subCut + " ";
			}
		} else {
			sub = in.substring(0, 1);
			sub = sub.toUpperCase();
			sub += in.substring(1);
		}
		return sub;
	}

	private final SoulEntry mSoul;
	private final BestiaryArea mParent;
	/* TODO: Use these to provide previous / next buttons */
	private final List<BestiaryEntryInterface> mPeers;
	private final int mPeerIndex;

	public BestiarySoulInventory(Player player, SoulEntry soul, BestiaryArea parent, boolean lowerInfoTier, List<BestiaryEntryInterface> peers, int peerIndex) {
		super(player, 36, LegacyComponentSerializer.legacySection().serialize(soul.getDisplayName()));

		mSoul = soul;
		mParent = parent;
		mPeers = peers;
		mPeerIndex = peerIndex;

		NBTTagCompound vars = soul.getNBT();
		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());
		AttributeContainer attr = ((MobNBT)entityNBT).getAttributes();;


		double armor = 0;
		double armorToughness = 0;
		double health = vars.hasKey("Health") ? 0.0 + Float.valueOf(vars.getFloat("Health")) : 0;
		double speed = vars.hasKey("MovementSpeed") ? 0.0 + Float.valueOf(vars.getFloat("MovementSpeed")) : 0.0;
		double damage = attr.getAttribute(AttributeType.ATTACK_DAMAGE) != null ? attr.getAttribute(AttributeType.ATTACK_DAMAGE).getBase() : 0.0;
		double speedScalar = 0;
		double speedPercent = 1;
		double bowDamage = 0;
		double explodePower = 0;
		double horseJumpPower = 0;
		double handDamage = 0;
		DamageType type = null;

		// Only need to create one of these
		EffectsVariable effectVar = new EffectsVariable("ActiveEffects");
		ItemsVariable itemsVar = new ItemsVariable("ArmorItems", new String[] {"Feet Equipment", "Legs Equipment", "Chest Equipment", "Head Equipment"});
		ItemsVariable handVar = new ItemsVariable("HandItems", new String[] {"Offhand", "Mainhand"});
		NBTVariable tagsVar = entityNBT.getVariable("Tags");
		// For each mob you want to work with:
		ItemStack[] armorItems = ((ItemsVariable)itemsVar.bind(entityNBT.getData())).getItems();
		ItemStack[] handItems = ((ItemsVariable)handVar.bind(entityNBT.getData())).getItems();
		String tagString = tagsVar.bind(entityNBT.getData()).get();
		if (armorItems != null) {
			for (ItemStack item : armorItems) {
				if (item != null && item.hasItemMeta()) {
					armor += getAttributeNumber(item, Attribute.GENERIC_ARMOR, ADD);

					if (mDefaultArmor.containsKey(item.getType()) && item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ARMOR) == null) {
						armor += mDefaultArmor.get(item.getType());
					}

					armorToughness += getAttributeNumber(item, Attribute.GENERIC_ARMOR_TOUGHNESS, ADD);
					speedScalar += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, ADD);
					speedPercent += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, SCALAR);
					damage += getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD);
				}
			}
		}

		if (handItems != null) {
			for (ItemStack item : handItems) {
				if (item != null && item.hasItemMeta()) {
					EquipmentSlot slot = handItems[0] != null && handItems[0].equals(item) ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND;
					if (item.getType().equals(Material.BOW) && slot == EquipmentSlot.HAND) {
						type = DamageType.RANGED;
						bowDamage += getBowDamage(item.getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
					}

					if (item.getType().equals(Material.CROSSBOW) && slot == EquipmentSlot.HAND) {
						type = DamageType.CROSSBOW;
					}

					if (item.getType().equals(Material.TRIDENT) && slot == EquipmentSlot.HAND) {
						type = DamageType.TRIDENT;
					}

					if (getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD, slot) == 0 && mDefaultItemDamage.containsKey(item.getType())) {
						damage += mDefaultItemDamage.get(item.getType());
					}

					if (handItems[0] != null && handItems[0].equals(item) && item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
						damage += ((0.0 + item.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2) + 0.5;
					}

					if (slot == EquipmentSlot.HAND) {
						horseJumpPower += getAttributeNumber(item, Attribute.HORSE_JUMP_STRENGTH, ADD, slot);
					}

					if (slot == EquipmentSlot.HAND) {
						handDamage += getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD, slot);
					}

					armor += getAttributeNumber(item, Attribute.GENERIC_ARMOR, ADD, slot);
					armorToughness += getAttributeNumber(item, Attribute.GENERIC_ARMOR_TOUGHNESS, ADD, slot);
					speedScalar += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, ADD, slot);
					speedPercent += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, SCALAR, slot);
					damage += getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD, slot);
				}
			}
		}

		// Does the mob attack primarily through explosions?
		if (entityNBT.getEntityType() == EntityType.GHAST) {
			type = DamageType.GHAST;
			explodePower = Double.valueOf(entityNBT.getVariable("ExplosionPower").get());
		} else if (entityNBT.getVariable("ExplosionRadius") != null) {
			type = DamageType.CREEPER;
			explodePower = Double.valueOf(entityNBT.getVariable("ExplosionRadius").get());
			if (entityNBT.getVariable("Powered") != null) {
				explodePower = Boolean.parseBoolean(entityNBT.getVariable("Powered").get()) ? explodePower * 2 : explodePower;
			}
		} else if (entityNBT.getEntityType() == EntityType.BLAZE) {
			type = DamageType.BLAZE;
		} else if (entityNBT.getEntityType() == EntityType.EVOKER) {
			type = DamageType.EVOKER;
		} else if (type == null && attr.getAttribute(AttributeType.ATTACK_DAMAGE) == null) {
			type = DamageType.MELEE;
			damage += mDefaultDamage.get(entityNBT.getEntityType());
		} else if (type == null) {
			type = DamageType.MELEE;
		} else {
			LibraryOfSouls.getInstance().getLogger().log(Level.INFO, "This mob somehow gained no type: " + soul.getName());
		}

		// Mojang.
		if (entityNBT.getEntityType() == EntityType.ZOMBIE || entityNBT.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
			armor += 2;
		}
		//This logic is in other methods, not because it repeats, but because its much easier to parse
		ItemStack armorItem = getArmorItem(armorItems[1], armor, armorToughness);

		ItemStack healthItem = getHealthItem(health);

		ItemStack damageItem = getDamageItem(handItems[0], damage, bowDamage, explodePower, horseJumpPower, handDamage, type);

		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(BestiaryAreaInventory.EMPTY_MAT));
		}

		if (lowerInfoTier) {
			// Lower tier of information
			_inventory.setItem(11, healthItem);
			_inventory.setItem(13, armorItem);
			_inventory.setItem(15, damageItem);
			_inventory.setItem(31, BestiaryAreaInventory.GO_BACK_ITEM);
		} else {
			// Higher teir of information
			ItemStack effectItem = ((EffectsVariable)effectVar.bind(entityNBT.getData())).getItem();
			effectItem = getEffectItem(effectItem);

			ItemStack speedItem = getSpeedItem(entityNBT, speed, speedScalar, speedPercent);

			ItemStack tagItem = getTagItem(tagString);

			ItemStack equipmentPageItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
			ItemMeta meta = equipmentPageItem.getItemMeta();
			meta.setDisplayName(ChatColor.GREEN + "View Equipment Items");
			equipmentPageItem.setItemMeta(meta);

			_inventory.setItem(2, healthItem);
			_inventory.setItem(4, armorItem);
			_inventory.setItem(6, damageItem);
			_inventory.setItem(13, equipmentPageItem);
			_inventory.setItem(20, speedItem);
			_inventory.setItem(22, effectItem);
			_inventory.setItem(24, tagItem);
			_inventory.setItem(31, BestiaryAreaInventory.GO_BACK_ITEM);
		}
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {
		/* Always cancel the event */
		event.setCancelled(true);

		/* Ignore non-left clicks */
		if (!event.getClick().equals(ClickType.LEFT)) {
			return;
		}

		Player player = (Player)event.getWhoClicked();
		int slot = event.getRawSlot();

		if (slot == 31 && event.getCurrentItem().getType().equals(BestiaryAreaInventory.GO_BACK_MAT)) {
			/* Go Back
			 * Note that parent's parent is passed as null here - must rely on the class to figure out its own parent
			 * That information isn't practical to determine here
			 */
			mParent.openBestiary(player, null, null, -1);
		} else if (slot == 13 && event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
			new BestiarySoulEquipmentInventory(player, mSoul, mParent, mPeers, mPeerIndex).openInventory(player, LibraryOfSouls.getInstance());
		}
	}

	// Use this one if you dont care about the slot
	public static double getAttributeNumber(ItemStack item, Attribute attribute, AttributeModifier.Operation operation) {
		return getAttributeNumber(item, attribute, operation, null);
	}

	// Use this one if you do care about the slot
	public static double getAttributeNumber(ItemStack item, Attribute attribute, AttributeModifier.Operation operation, EquipmentSlot slot) {
		ItemMeta meta = item.getItemMeta();
		double attributeNum = 0;
		if (meta.getAttributeModifiers(attribute) != null) {
			Iterator<AttributeModifier> iterator = meta.getAttributeModifiers(attribute).iterator();
			while (iterator.hasNext()) {
				AttributeModifier mod = iterator.next();
				if (mod.getOperation().equals(operation)) {
					if (slot == null) {
						attributeNum += mod.getAmount();
					} else if (mod.getSlot() == slot) {
						attributeNum += mod.getAmount();
					}
				}
			}
		}
		return attributeNum;
	}

	private static ItemStack getHealthItem(double health) {
		ItemStack healthItem = new ItemStack(Material.GLISTERING_MELON_SLICE);
		ItemMeta healthMeta = healthItem.getItemMeta();
		List<String> lore = new ArrayList<>();

		healthMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		healthMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		lore.add(ChatColor.RED + "" + health + " Max Health");
		healthMeta.setLore(lore);
		healthMeta.setDisplayName(ChatColor.RED + "Health");
		healthItem.setItemMeta(healthMeta);

		return healthItem;
	}

	private static ItemStack getArmorItem(ItemStack item, double armor, double armorToughness) {
		ItemStack armorItem = item != null && item.getItemMeta() != null ? item : new ItemStack(Material.IRON_CHESTPLATE);
		ItemMeta armorMeta = armorItem.getItemMeta();
		List<String> lore = new ArrayList<>();

		armorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

		if (armorToughness != 0) {
			lore.add(ChatColor.BLUE + "" + armorToughness + " Armor");
		}

		if (armor == 0) {
			lore.add(ChatColor.DARK_RED + "" + armor + " Armor");
		} else {
			lore.add(ChatColor.BLUE + "" + armor + " Armor");
		}

		armorMeta.setLore(lore);
		armorMeta.setDisplayName(ChatColor.WHITE + "Armor");

		if (armor != 0 || armorToughness != 0) {
			armorMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		}
		armorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		armorItem.setItemMeta(armorMeta);

		return armorItem;
	}

	private static ItemStack getDamageItem(ItemStack item, double damage, double bowDamage, double explodePower, double horseJumpPower, double handDamage, DamageType type) {
		ItemStack damageItem = item;
		if (damageItem == null || damageItem.getItemMeta() == null) {
			if (type == DamageType.RANGED) {
				damageItem = new ItemStack(Material.BOW);
			} else if (type == DamageType.TRIDENT) {
				damageItem = new ItemStack(Material.TRIDENT);
			} else if (type == DamageType.CROSSBOW) {
				damageItem = new ItemStack(Material.CROSSBOW);
			} else if (type == DamageType.GHAST || type == DamageType.CREEPER) {
				damageItem = new ItemStack(Material.GUNPOWDER);
			} else if (type == DamageType.EVOKER) {
				damageItem = new ItemStack(Material.TOTEM_OF_UNDYING);
			} else if (type == DamageType.BLAZE) {
				damageItem = new ItemStack(Material.BLAZE_POWDER);
			} else {
				damageItem = new ItemStack(Material.IRON_SWORD);
			}
		}

		ItemMeta damageMeta = damageItem.getItemMeta();
		damageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<String> lore = new ArrayList<>();

		if (type == DamageType.RANGED && damage != 0) {
			lore.add(ChatColor.DARK_GREEN + " " + damage + " Attack Damage");
			lore.add(ChatColor.DARK_GREEN + " " + bowDamage + " Projectile Damage");
		} else if (type == DamageType.RANGED) {
			lore.add(ChatColor.DARK_GREEN + " " + bowDamage + " Projectile Damage");
		} else if (type == DamageType.TRIDENT) {
			if (handDamage > 0) {
				lore.add(ChatColor.DARK_GREEN + " " + (handDamage + 1) + " Thrown Damage");
			} else {
				lore.add(ChatColor.DARK_GREEN + " 8 Thrown Damage");
			}
		} else if (type == DamageType.CROSSBOW) {
			if (handDamage > 0) {
				lore.add(ChatColor.DARK_GREEN + " " + (handDamage + 1) + " Projectile Damage");
			} else {
				lore.add(ChatColor.DARK_GREEN + " 4 Projectile Damage");
			}
		} else if (type == DamageType.EVOKER) {
			if (handDamage > 0) {
				lore.add(ChatColor.DARK_GREEN + " " + (handDamage + 1) + " Fang Damage");
			} else {
				lore.add(ChatColor.DARK_GREEN + " 6 Fang Damage");
			}
		} else if (type == DamageType.BLAZE) {
			if (horseJumpPower > 0) {
				lore.add(ChatColor.DARK_GREEN + " " + (horseJumpPower + 1) + " Fireball Damage");
			} else {
				lore.add(ChatColor.DARK_GREEN + " 5 Fireball Damage");
			}
		} else if (type == DamageType.CREEPER || type == DamageType.GHAST) {
			lore.add(ChatColor.DARK_GREEN + " " + explodePower + " Explosion Power");
		} else if (type == DamageType.MELEE) {
			lore.add(ChatColor.DARK_GREEN + " " + damage + " Attack Damage");
		}


		damageMeta.setLore(lore);
		damageMeta.setDisplayName(ChatColor.WHITE + "Damage");

		damageMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		damageMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		damageItem.setItemMeta(damageMeta);

		return damageItem;
	}

	private static ItemStack getEffectItem(ItemStack effectItem) {
		List<String> lore = new ArrayList<>();
		if (effectItem != null && effectItem.hasItemMeta()) {
			PotionMeta potionMeta = (PotionMeta)effectItem.getItemMeta();

			for (PotionEffect effect : potionMeta.getCustomEffects()) {
				lore.add(ChatColor.DARK_BLUE + formatWell(effect.toString().substring(0, effect.toString().indexOf(":")).toLowerCase()) + " (∞)");
			}

			potionMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
			potionMeta.setLore(lore);
			potionMeta.setDisplayName(ChatColor.WHITE + "Effects");
			potionMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
			effectItem.setItemMeta(potionMeta);
		} else {
			effectItem = new ItemStack(Material.POTION);
			PotionMeta potionMeta = (PotionMeta)effectItem.getItemMeta();
			potionMeta.setBasePotionData(new PotionData(PotionType.WATER));
			potionMeta.setDisplayName(ChatColor.WHITE + "Effects");

			potionMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
			potionMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

			effectItem.setItemMeta(potionMeta);
		}

		return effectItem;
	}

	private static ItemStack getSpeedItem(EntityNBT entityNBT, double speed, double speedScalar, double speedPercent) {
		ItemStack speedItem = new ItemStack(Material.SUGAR);
		ItemMeta speedMeta = speedItem.getItemMeta();
		List<String> lore = new ArrayList<>();

		if (mDefaultSpeed.containsKey(entityNBT.getEntityType()) && speed == 0) {
			speed = mDefaultSpeed.get(entityNBT.getEntityType());
			speed += speedScalar;
			speed *= speedPercent;
		} else if (entityNBT.getEntityType().equals(EntityType.SLIME)) {
			int size = Integer.valueOf(entityNBT.getVariable("Size").get());
			speed = 0.2 + (0.1 * size);
			speed += speedScalar;
			speed *= speedPercent;
		} else {
			speed += speedScalar;
			speed *= speedPercent;
		}

		lore.add(ChatColor.GREEN + "" + speed + " Speed");
		speedMeta.setLore(lore);
		speedMeta.setDisplayName(ChatColor.WHITE + "Speed");

		speedMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		speedMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		speedItem.setItemMeta(speedMeta);

		return speedItem;
	}

	private static ItemStack getTagItem(String tagString) {
		ItemStack tagItem = new ItemStack(Material.NAME_TAG);
		ItemMeta tagMeta = tagItem.getItemMeta();
		List<String> lore = new ArrayList<>();

		if (tagString != null) {
			List<String> tags = formatTags(tagString);
			for (String tag : tags) {
				lore.add(ChatColor.GRAY + tag);
			}
		} else {
			lore.add(ChatColor.GRAY + "No Tags!");
		}
		tagMeta.setLore(lore);
		tagMeta.setDisplayName(ChatColor.WHITE + "Tags");

		tagMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		tagMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		tagItem.setItemMeta(tagMeta);

		return tagItem;
	}

	public static List<String> formatTags(String tag) {
		List<String> ret = new ArrayList<>();

		if (tag.contains(",")) {
			String[] tags = tag.split(",");
			for (String iterTag : tags) {
				iterTag = iterTag.replaceAll("\"", "");
				iterTag = iterTag.replaceAll("\\[", "");
				iterTag = iterTag.replaceAll("\\]", "");
				iterTag = formatWell(iterTag);
				iterTag = iterTag.replaceAll("Boss ", "");
				ret.add(iterTag);
			}
		} else {
			tag = tag.replaceAll("\"", "");
			tag = tag.replaceAll("\\[", "");
			ret.add(tag);
		}

		return ret;
	}

	public static double getBowDamage(int powerLevel) {
		int adjust = 0;

		if (powerLevel < 6) {
			adjust = 1;
		}

		return Math.ceil((0.0006633685369 * powerLevel * powerLevel) + (0.7553196723 * powerLevel) + 4.97314159) - adjust;
	}
}
