package cn.daniellee.plugin.eb.component;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Objects;

/**
 * 物品生成实用类
 */
public class ItemGenerator {

	public static boolean disableSkullLoad = false;

	public static ItemStack getItem(String name, List<String> lore, String m, int durability) {
		Material material = Material.getMaterial(m.toUpperCase());
		if (material == null) material = Material.STONE;
		ItemStack itemStack = new ItemStack(material);
		itemStack.setDurability((short) durability);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta != null) {
			itemMeta.setDisplayName(name.replace("&", "§"));
			if (lore != null && !lore.isEmpty()) {
				for (int i = 0; i < lore.size(); i++) lore.set(i, lore.get(i).replace("&", "§"));
				itemMeta.setLore(lore);
			}
			itemStack.setItemMeta(itemMeta);
		}
		return itemStack;
	}

	public static ItemStack getTag(String name, List<String> lore, boolean enchant) {
		ItemStack is = new ItemStack(Material.NAME_TAG);
		ItemMeta meta = is.getItemMeta();
		meta.setDisplayName(name);
		meta.setLore(lore);
		if (enchant) {
			meta.addEnchant(Enchantment.DURABILITY, 1, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		is.setItemMeta(meta);
		return is;
	}

	public static ItemStack getSkullItem(String owner, String name, List<String> lore) {
		Material material = Material.getMaterial("PLAYER_HEAD");
		if (material == null) material = Material.getMaterial("SKULL_ITEM");
		ItemStack itemStack = new ItemStack(Objects.requireNonNull(material));
		itemStack.setDurability((short) 3);
		SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
		if (skullMeta != null) {
			if (!disableSkullLoad) skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));
			skullMeta.setDisplayName(name.replace("&", "§"));
			if (lore != null && !lore.isEmpty()) {
				for (int i = 0; i < lore.size(); i++) lore.set(i, lore.get(i).replace("&", "§"));
				skullMeta.setLore(lore);
			}
			itemStack.setItemMeta(skullMeta);
		}
		return itemStack;
	}
}
