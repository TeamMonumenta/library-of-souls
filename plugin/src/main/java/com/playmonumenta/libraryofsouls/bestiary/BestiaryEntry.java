package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.goncalomb.bukkit.mylib.reflect.NBTTagCompound;
import com.goncalomb.bukkit.mylib.utils.CustomInventory;
import com.goncalomb.bukkit.nbteditor.nbt.EntityNBT;
import com.goncalomb.bukkit.nbteditor.nbt.variables.EffectsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.NBTVariable;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.SoulEntry;

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

import net.md_5.bungee.api.ChatColor;

public class BestiaryEntry extends CustomInventory {
	private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_NUMBER;
	private static final AttributeModifier.Operation SCALAR = AttributeModifier.Operation.ADD_SCALAR;

	private final SoulEntry mSoul;
	private final BestiaryEntryInterface mParent;

	public BestiaryEntry(Player player, SoulEntry soul, BestiaryEntryInterface parent, boolean lowerInfoTier) {
		super(player, 36, soul.getDisplayName());

		mSoul = soul;
		mParent = parent;

		NBTTagCompound vars = soul.getNBT();
		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());

		double armor = 0;
		double armorToughness = 0;
		double health = vars.hasKey("Health") ? 0.0 + Float.valueOf(vars.getFloat("Health")) : 0;
		double speed = vars.hasKey("MovementSpeed") ? 0.0 + Float.valueOf(vars.getFloat("MovementSpeed")) : 0.0;
		double damage = entityNBT != null && entityNBT.getVariable("AttackDamage") != null ? 0.0 + Double.valueOf(entityNBT.getVariable("AttackDamage").get()) : 0.0;
		double speedScalar = 0;
		double speedPercent = 1;
		double bowDamage = 0;
		double explodePower = 0;
		boolean ranged = false;
		boolean trident = false;
		boolean explode = false;

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

					if (BestiaryUtils.mDefaultArmor.containsKey(item.getType()) && item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ARMOR) == null) {
						armor += BestiaryUtils.mDefaultArmor.get(item.getType());
					}

					armorToughness += getAttributeNumber(item, Attribute.GENERIC_ARMOR_TOUGHNESS, ADD);
					health += getAttributeNumber(item, Attribute.GENERIC_MAX_HEALTH, ADD);
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
						ranged = true;
						bowDamage += getBowDamage(item.getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
					}

					trident = item.getType().equals(Material.TRIDENT) && slot == EquipmentSlot.HAND;

					if (getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD, slot) == 0 && BestiaryUtils.mDefaultItemDamage.containsKey(item.getType())) {
						damage += BestiaryUtils.mDefaultItemDamage.get(item.getType());
					}

					if (handItems[0] != null && handItems[0].equals(item) && item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
						damage += ((0.0 + item.getEnchantmentLevel(Enchantment.DAMAGE_ALL)) / 2) + 0.5;
					}

					armor += getAttributeNumber(item, Attribute.GENERIC_ARMOR, ADD);
					armorToughness += getAttributeNumber(item, Attribute.GENERIC_ARMOR_TOUGHNESS, ADD);
					health += getAttributeNumber(item, Attribute.GENERIC_MAX_HEALTH, ADD);
					speedScalar += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, ADD);
					speedPercent += getAttributeNumber(item, Attribute.GENERIC_MOVEMENT_SPEED, SCALAR);
					damage += getAttributeNumber(item, Attribute.GENERIC_ATTACK_DAMAGE, ADD);
				}
			}
		}

		// Does the mob attack primarily through explosions?
		if (entityNBT.getVariable("ExplosionPower") != null) {
			explode = true;
			explodePower = Double.valueOf(entityNBT.getVariable("ExplosionPower").get());
		} else if (!ranged && !trident) {
			damage += BestiaryUtils.mDefaultDamage.get(entityNBT.getEntityType());
		}
		// Mojang.
		if (entityNBT.getEntityType() == EntityType.ZOMBIE || entityNBT.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
			armor += 2;
		}
		//This logic is in other methods, not because it repeats, but because its much easier to parse
		ItemStack armorItem = getArmorItem(armorItems[1], armor, armorToughness);

		ItemStack healthItem = getHealthItem(health);

		ItemStack damageItem = getDamageItem(handItems[0], damage, bowDamage, explodePower, ranged, trident, explode);

		for (int i = 0; i < 36; i++) {
			_inventory.setItem(i, new ItemStack(BestiaryEntryContainerInventory.EMPTY_MAT));
		}

		if (lowerInfoTier) {
			// Lower tier of information
			_inventory.setItem(11, healthItem);
			_inventory.setItem(13, armorItem);
			_inventory.setItem(15, damageItem);
			_inventory.setItem(31, BestiaryEntryContainerInventory.GO_BACK_ITEM);
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
			_inventory.setItem(31, BestiaryEntryContainerInventory.GO_BACK_ITEM);
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

		if (slot == 31 && event.getCurrentItem().getType().equals(BestiaryEntryContainerInventory.GO_BACK_MAT)) {
			/* Go Back
			 * Note that parent's parent is passed as null here - must rely on the class to figure out its own parent
			 * That information isn't practical to determine here
			 */
			mParent.openBestiary(player, null);
		} else if (slot == 13 && event.getCurrentItem().getType().equals(Material.LIME_STAINED_GLASS_PANE)) {
			new EquipmentDisplay(player, mSoul, mParent, mParent.getBestiaryParent()).openInventory(player, LibraryOfSouls.getInstance());
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

		armorMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
		armorMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

		armorItem.setItemMeta(armorMeta);

		return armorItem;
	}

	private static ItemStack getDamageItem(ItemStack item, double damage, double bowDamage, double explodePower, boolean ranged, boolean trident, boolean explode) {
		ItemStack damageItem = item;
		if (damageItem == null || damageItem.getItemMeta() == null) {
			if (ranged) {
				damageItem = new ItemStack(Material.BOW);
			} else if (trident) {
				damageItem = new ItemStack(Material.TRIDENT);
			} else if (explode) {
				damageItem = new ItemStack(Material.GUNPOWDER);
			} else {
				damageItem = new ItemStack(Material.IRON_SWORD);
			}
		}


		ItemMeta damageMeta = damageItem.getItemMeta();
		damageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		List<String> lore = new ArrayList<>();

		if (ranged && damage != 0) {
			lore.add(ChatColor.DARK_GREEN + " " + damage + " Attack Damage");
			lore.add(ChatColor.DARK_GREEN + " " + bowDamage + " Projectile Damage");
		} else if (ranged) {
			lore.add(ChatColor.DARK_GREEN + " " + bowDamage + " Projectile Damage");
		} else if (trident) {
			lore.add(ChatColor.DARK_GREEN + " " + damage + "Thrown Damage");
		} else if (explode) {
			lore.add(ChatColor.DARK_GREEN + " " + explodePower + "Explosion Power");
		} else {
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
				lore.add(ChatColor.DARK_BLUE + BestiaryUtils.formatWell(effect.toString().substring(0, effect.toString().indexOf(":")).toLowerCase()) + " (∞)");
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

		if (BestiaryUtils.mDefaultSpeed.containsKey(entityNBT.getEntityType()) && speed == 0) {
			speed = BestiaryUtils.mDefaultSpeed.get(entityNBT.getEntityType());
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
				iterTag = BestiaryUtils.formatWell(iterTag);
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
