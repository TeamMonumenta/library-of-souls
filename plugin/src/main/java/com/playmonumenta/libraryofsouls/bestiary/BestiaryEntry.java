package com.playmonumenta.libraryofsouls.bestiary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
import com.goncalomb.bukkit.nbteditor.nbt.variables.EffectsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.ItemsVariable;
import com.goncalomb.bukkit.nbteditor.nbt.variables.NBTVariable;
import com.playmonumenta.libraryofsouls.LibraryOfSouls;
import com.playmonumenta.libraryofsouls.Soul;
import com.playmonumenta.libraryofsouls.SoulEntry;
import com.playmonumenta.libraryofsouls.SoulsDatabase;

import net.md_5.bungee.api.ChatColor;

public class BestiaryEntry extends CustomInventory {
	private static final AttributeModifier.Operation ADD = AttributeModifier.Operation.ADD_NUMBER;
	private static final AttributeModifier.Operation SCALAR = AttributeModifier.Operation.ADD_SCALAR;
	private HashMap<SoulEntry, Integer> mAvailableMobs;
	private List<SoulEntry> mSouls;
	private int mCurrentSoul;
	private String mTitle;
	public BestiaryEntry (Soul soul, Player player, boolean lessInfo, String title, int currentSoul, HashMap<SoulEntry, Integer> availableMobs) {
		super(player, 27, BestiaryUtils.hashColor(title) + soul.getPlaceholder().getItemMeta().getDisplayName());
		mSouls = SoulsDatabase.getInstance().getSoulsByLocation(title);
		mAvailableMobs = availableMobs;
		mCurrentSoul = currentSoul;
		mTitle = title;
		generateBestiaryEntry(soul, player, lessInfo);
	}

	private void generateBestiaryEntry(Soul soul, Player player, boolean lessInfo) {
		NBTTagCompound vars = soul.getNBT();
		EntityNBT entityNBT = EntityNBT.fromEntityData(soul.getNBT());

		double armor = 0;
		double armorToughness = 0;
		float health = Float.valueOf(vars.getFloat("Health"));
		double speed = vars.hasKey("MovementSpeed") ? 0.0 + Float.valueOf(vars.getFloat("MovementSpeed")) : 0.0;
		double damage = 0;
		double speedScalar = 0;
		double speedPercent = 1;
		double bowDamage = 0;
		double explodePower = 0;
		boolean ranged = false;
		boolean trident = false;
		boolean explode = false;

		// Only need to create one of these
		EffectsVariable effectVar = new EffectsVariable("ActiveEffects");
		ItemsVariable itemsVar = new ItemsVariable("ArmorItems", new String[] { "Feet Equipment", "Legs Equipment", "Chest Equipment", "Head Equipment" });
		ItemsVariable handVar = new ItemsVariable("HandItems", new String[] {"Offhand", "Mainhand"});
		NBTVariable tagsVar = entityNBT.getVariable("Tags");
		// For each mob you want to work with:
		ItemStack effectItem = ((EffectsVariable)effectVar.bind(entityNBT.getData())).getItem();
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
					if (item.getType().equals(Material.BOW)) {
						ranged = true;
						bowDamage += getBowDamage(item.getEnchantmentLevel(Enchantment.ARROW_DAMAGE));
					}

					if (item.getType().equals(Material.TRIDENT)) {
						trident = true;
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

		if (entityNBT.getVariable("ExplosionPower") != null) {
			explode = true;
			explodePower = Double.valueOf(entityNBT.getVariable("ExplosionPower").get());
		} else if (!ranged && !trident) {
			damage += BestiaryUtils.mDefaultDamage.get(entityNBT.getEntityType());
		}

		ItemStack armorItem = getArmorItem(armor, armorToughness);

		ItemStack healthItem = getHealthItem(health);

		ItemStack damageItem = getDamageItem(damage, bowDamage, explodePower, ranged, trident, explode);

		ItemStack prevPageItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		ItemMeta meta = prevPageItem.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Previous Page");
		prevPageItem.setItemMeta(meta);

		ItemStack nextPageItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
		meta = nextPageItem.getItemMeta();
		meta.setDisplayName(ChatColor.BLUE + "Next Page");
		nextPageItem.setItemMeta(meta);
//		for (int i = 0; i <= 100; i++) {
//			Bukkit.broadcastMessage(i + ": " + getBowDamage(i));
//		}

		if (lessInfo) {
			for (int i = 0; i < 27; i++) {
				_inventory.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
			}
			_inventory.setItem(11, healthItem);
			_inventory.setItem(13, armorItem);
			_inventory.setItem(15, damageItem);
			_inventory.setItem(18, prevPageItem);
			_inventory.setItem(26, nextPageItem);
			return;
		}

		effectItem = getEffectItem(effectItem);

		ItemStack speedItem = getSpeedItem(entityNBT, speed, speedScalar, speedPercent);

		ItemStack tagItem = getTagItem(tagString);

		for (int i = 0; i < 27; i++) {
			_inventory.setItem(i, new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE));
		}
		_inventory.setItem(2, healthItem);
		_inventory.setItem(4, armorItem);
		_inventory.setItem(6, damageItem);
		_inventory.setItem(20, speedItem);
		_inventory.setItem(22, effectItem);
		_inventory.setItem(24, tagItem);
		_inventory.setItem(18, prevPageItem);
		_inventory.setItem(26, nextPageItem);
	}

	@Override
	protected void inventoryClick(final InventoryClickEvent event) {
		int slot = event.getRawSlot();
		Player player = (Player)event.getWhoClicked();
		if (mSouls != null && mAvailableMobs != null && slot == 18 && event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
			for (int i = mCurrentSoul - 1; i >= 0; i--) {
				Soul soul = mSouls.get(i);
				if (mAvailableMobs.containsKey(soul)) {
					if (mAvailableMobs.get(soul) >= 2 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
						new BestiaryEntry(soul, player, false, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 1 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
						new BestiaryEntry(soul, player, true, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 3 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, false, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, true, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 10) {
						new BestiaryEntry(soul, player, false, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 5) {
						new BestiaryEntry(soul, player, true, mTitle, i, mAvailableMobs).openInventory(player, LibraryOfSouls.getInstance());
						break;
					}
				}
			}
		} else if (mSouls != null && mAvailableMobs != null && slot == 26 && event.getCurrentItem().getType().equals(Material.GREEN_STAINED_GLASS_PANE)) {
			for (int i = mCurrentSoul + 1; i < mSouls.size(); i++) {
				Soul soul = mSouls.get(i);
				if (mAvailableMobs.containsKey(soul)) {
					if (mAvailableMobs.get(soul) >= 2 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
						new BestiaryEntry(soul, player, false, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 1 && soul.getNBT().getString("Tags").contains("\"Boss\"")) {
						new BestiaryEntry(soul, player, true, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 3 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, false, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 5 && soul.getNBT().getString("Tags").contains("\"Elite\"")) {
						new BestiaryEntry(soul, player, true, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 10) {
						new BestiaryEntry(soul, player, false, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					} else if (mAvailableMobs.get(soul) >= 5) {
						new BestiaryEntry(soul, player, true, mTitle, i).openInventory(player, LibraryOfSouls.getInstance());
						break;
					}
				}
			}
		}
		event.setCancelled(true);
	}

	public static double getAttributeNumber(ItemStack item, Attribute attribute, AttributeModifier.Operation operation) {
		ItemMeta meta = item.getItemMeta();
		double attributeNum = 0;
		if (meta.getAttributeModifiers(attribute) != null) {
			Iterator<AttributeModifier> iterator = meta.getAttributeModifiers(attribute).iterator();
			while (iterator.hasNext()) {
				AttributeModifier mod = iterator.next();
				if (mod.getOperation().equals(operation)) {
					attributeNum += mod.getAmount();
				}
			}
		}
		return attributeNum;
	}

	private static ItemStack getHealthItem(double health) {
		ItemStack healthItem = new ItemStack(Material.GLISTERING_MELON_SLICE);
		ItemMeta healthMeta = healthItem.getItemMeta();
		List<String> lore = new ArrayList<>();

		lore.add(ChatColor.RED + "" + health + " Max Health");
		healthMeta.setLore(lore);
		healthMeta.setDisplayName(ChatColor.RED + "Health");
		healthItem.setItemMeta(healthMeta);

		return healthItem;
	}

	private static ItemStack getArmorItem(double armor, double armorToughness) {
		ItemStack armorItem = new ItemStack(Material.IRON_CHESTPLATE);
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
		armorItem.setItemMeta(armorMeta);

		return armorItem;
	}

	private static ItemStack getDamageItem(double damage, double bowDamage, double explodePower, boolean ranged, boolean trident, boolean explode) {
		ItemStack damageItem;

		if (ranged) {
			damageItem = new ItemStack(Material.BOW);
		} else if (trident) {
			damageItem = new ItemStack(Material.TRIDENT);
		} else if (explode) {
			damageItem = new ItemStack(Material.GUNPOWDER);
		} else {
			damageItem = new ItemStack(Material.IRON_SWORD);
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
			effectItem.setItemMeta(potionMeta);
		} else {
			effectItem = new ItemStack(Material.POTION);
			PotionMeta potionMeta = (PotionMeta)effectItem.getItemMeta();
			potionMeta.setBasePotionData(new PotionData(PotionType.WATER));
			potionMeta.setDisplayName(ChatColor.WHITE + "Effects");
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
		tagItem.setItemMeta(tagMeta);

		return tagItem;
	}

	public static List<String> formatTags(String tag) {
		List<String> ret = new ArrayList<>();

		if (tag.contains(",")) {
			String[] tags = tag.split(",");
			for (String iterTag : tags) {
				Bukkit.broadcastMessage(iterTag);
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
