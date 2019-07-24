package cn.daniellee.plugin.eb.menu;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.component.ItemGenerator;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.filter.TaggedMenu;
import cn.daniellee.plugin.eb.menu.sort.SortRule;
import cn.daniellee.plugin.eb.model.Building;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.text.SimpleDateFormat;
import java.util.*;

import static cn.daniellee.plugin.eb.ExcellentBuilding.instance;

public class BuildingMenu implements InventoryHolder, TaggedMenu {

	private Player player;
	private int page;
	private TagHandler tags;
	private SortRule sortRule;
	private final Inventory inventory;

	@Override
	public Inventory getInventory() {
		return inventory;
	}

	public void setFilter(TagHandler tags) {
		this.tags = tags;
	}

	public void setSortRule(SortRule sortRule) {
		this.sortRule = sortRule;
	}

	public TagHandler getTags() {
		return tags;
	}

	public SortRule getSortRule() {
		return sortRule;
	}

	public BuildingMenu(Player player, int page, TagHandler tags, SortRule sort) {
		this.player = player;
		this.page = page;
		this.tags = tags;
		this.sortRule = sort;
		int size = 54;
		inventory = Bukkit.createInventory(this, size, ExcellentBuilding.getInstance().getConfig().getString("menu.building.title", "&6&lExcellent building").replace("&", "§"));
		refresh();
	}
	public static Inventory generate(Player player, int page, TagHandler tags, SortRule sort) {
		return new BuildingMenu(player, page, tags, sort).getInventory();
	}

	@Override
	public void refresh() {
		inventory.clear();
		List<Building> buildings = BuildingCore.getAndSortReviewedBuildings(tags, sortRule);
		List<String> lore = ExcellentBuilding.getInstance().getConfig().getStringList("menu.building.button.lore");
		List<String> operationLore = ExcellentBuilding.getInstance().getConfig().getStringList("menu.building.button.operation-lore");
		List<String> ownerLore = ExcellentBuilding.getInstance().getConfig().getStringList("menu.building.button.owner-lore");
		SimpleDateFormat format = new SimpleDateFormat(ExcellentBuilding.getInstance().getConfig().getString("menu.common.date-format", "yyyy-MM-dd"));
		for (int i = 0; i < 45; i++) {
			int targetIndex = i + 45 * (page - 1);
			if (targetIndex >= buildings.size()) break;
			Building building = buildings.get(targetIndex);
			List<String> targetLore = new ArrayList<>(lore);
			targetLore.replaceAll(s -> s.replace("{player}", building.getPlayer()).replace("{likes}", Integer.toString(building.getLikes())).replace("{createDate}", format.format(new Date(building.getCreateDate()))).replace("{empty}", building.getIntroduction() != null && !"".equals(building.getIntroduction()) ? "" : ExcellentBuilding.getInstance().getConfig().getString("menu.empty-intro", "Empty")));
			if (building.getIntroduction() != null && !"".equals(building.getIntroduction())) {
				List<String> introduction = new Gson().fromJson(building.getIntroduction(), new TypeToken<List<String>>(){}.getType());
				introduction.replaceAll(s -> "&r" + s);
				targetLore.addAll(introduction);
			}
			targetLore.addAll(operationLore);
			if (player.getName().equals(building.getPlayer()) || player.hasPermission("building.modify.force")) targetLore.addAll(ownerLore);
			if (building.getIconMaterial() != null && !"".equals(building.getIconMaterial())) {
				inventory.setItem(i, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.building.button.name", "&bBuilding name: &d{name} &7({id})").replace("{name}", building.getName()).replace("{id}", building.getId()), targetLore, building.getIconMaterial(), building.getIconDurability()));
			} else {
				inventory.setItem(i, ItemGenerator.getSkullItem(building.getPlayer(), ExcellentBuilding.getInstance().getConfig().getString("menu.building.button.name", "&bBuilding name: &d{name} &7({id})").replace("{name}", building.getName()).replace("{id}", building.getId()), targetLore));
			}
		}

		inventory.setItem(49, ItemGenerator.getItem(ChatColor.RED + "关闭页面", null, Material.BARRIER.name(), 0));
		inventory.setItem(48, ItemGenerator.getItem(ChatColor.GOLD + "排序方式", Arrays.asList(ChatColor.YELLOW + sortRule.getName(), ChatColor.AQUA + "点击切换"), Material.HOPPER.name(), 0));
		inventory.setItem(50, ItemGenerator.getItem(ChatColor.GOLD + "标签检索", Arrays.asList(ChatColor.YELLOW + "当前检索: " + ChatColor.WHITE + tags, ChatColor.AQUA + "点击设置"), Material.NAME_TAG.name(), 0));

		if (buildings.size() > 45) {
			// 上一页按钮
			if (page > 1) {
				inventory.setItem(45, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.common.back.name", "&eBack to {page}").replace("{page}", "[" + (page - 1) + "]"), null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.back.item.material", "RED_WOOL"), ExcellentBuilding.getInstance().getConfig().getInt("menu.common.back.item.durability", 0)));
			}
			// 下一页按钮
			if (45 * page < buildings.size()) {
				inventory.setItem(53, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.common.next.name", "&eNext to {page}").replace("{page}", "[" + (page + 1) + "]"), null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.next.item.material", "LIME_WOOL"), ExcellentBuilding.getInstance().getConfig().getInt("menu.common.next.item.durability", 0)));
			}
		}
	}

	public void handleChange(Player player, int slot) {
		if (slot == 48) {
			this.sortRule = SortRule.values()[(sortRule.ordinal() + 1) % SortRule.values().length];
			refresh();
		} else if (slot == 50) {
			Bukkit.getScheduler().runTask(instance, () -> player.openInventory(new TagEditMenu(tags, this).getInventory()));
		} else {
			player.closeInventory();
		}
	}
}
