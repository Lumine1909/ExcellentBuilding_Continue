package cn.daniellee.plugin.eb.menu;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.component.ItemGenerator;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.filter.TaggedMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;

import static cn.daniellee.plugin.eb.ExcellentBuilding.instance;

public class EditMenu implements InventoryHolder, TaggedMenu {

	private final Inventory menu;
	private final TagHandler tags;
	private final String id;

	public EditMenu(String id, TagHandler tags) {
		this.tags = tags;
		this.id = id;
		menu = Bukkit.createInventory(this, 9, ExcellentBuilding.getInstance().getConfig().getString("menu.edit.title", "&6&lEdit building").replace("&", "§"));
		refresh();
	}

	public static Inventory generate(String id, TagHandler tags) {
		return new EditMenu(id, tags).getInventory();
	}

	@Override
	public Inventory getInventory() {
		return menu;
	}

	public TagHandler getTags() {
		return tags;
	}

	@Override
	public void refresh() {
		instance.getStorage().updateBuilding(id, "tags", tags.serializeTags());
		menu.setItem(1, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.name.name", "&bModify name"), ExcellentBuilding.getInstance().getConfig().getStringList("menu.edit.button.name.lore"), ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.name.item.material", "NAME_TAG"), ExcellentBuilding.getInstance().getConfig().getInt("menu.edit.button.name.item.durability", 0)));
		menu.setItem(2, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.intro.name", "&bModify introduction"), ExcellentBuilding.getInstance().getConfig().getStringList("menu.edit.button.intro.lore"), ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.intro.item.material", "WRITABLE_BOOK"), ExcellentBuilding.getInstance().getConfig().getInt("menu.edit.button.intro.item.durability", 0)));
		menu.setItem(3, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.location.name", "&bReset teleport point"), ExcellentBuilding.getInstance().getConfig().getStringList("menu.edit.button.location.lore"), ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.location.item.material", "ENDER_PEARL"), ExcellentBuilding.getInstance().getConfig().getInt("menu.edit.button.location.item.durability", 0)));
		menu.setItem(4, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.icon.name", "&bModify icon"), ExcellentBuilding.getInstance().getConfig().getStringList("menu.edit.button.icon.lore"), ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.icon.item.material", "BEACON"), ExcellentBuilding.getInstance().getConfig().getInt("menu.edit.button.icon.item.durability", 0)));
		menu.setItem(5, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.delete.name", "&bDelete building"), ExcellentBuilding.getInstance().getConfig().getStringList("menu.edit.button.delete.lore"), ExcellentBuilding.getInstance().getConfig().getString("menu.edit.button.delete.item.material", "TNT"), ExcellentBuilding.getInstance().getConfig().getInt("menu.edit.button.delete.item.durability", 0)));
		menu.setItem(6, ItemGenerator.getItem(ChatColor.GOLD + "设置标签", Arrays.asList(ChatColor.YELLOW + "当前标签: " + ChatColor.WHITE + tags, ChatColor.AQUA + "点击设置"), Material.NAME_TAG.name(), 0));
	}
}
