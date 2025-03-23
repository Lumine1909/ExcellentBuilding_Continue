package cn.daniellee.plugin.eb.menu;

import cn.daniellee.plugin.eb.component.ItemGenerator;
import cn.daniellee.plugin.eb.menu.filter.BuildingTag;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.filter.TaggedMenu;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

import static cn.daniellee.plugin.eb.ExcellentBuilding.instance;

public class TagEditMenu implements InventoryHolder {

    private final TaggedMenu prevMenu;
    private final Inventory inventory;
    private final TagHandler tags;

    public TagEditMenu(TagHandler tags, TaggedMenu prevMenu) {
        this.tags = tags;
        this.prevMenu = prevMenu;
        inventory = Bukkit.createInventory(this, 54);
        inventory.setItem(53, ItemGenerator.getItem(ChatColor.AQUA + "返回", null, Material.BARRIER.name(), 0));
        BuildingTag.fillInventory(inventory, tags);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void handleClick(Player player, int slot) {
        if (slot == 53) {
            callBack(player);
            return;
        }
        ItemStack is = inventory.getItem(slot);
        if (is == null || is.getType() == Material.AIR) {
            return;
        }
        BuildingTag tag = BuildingTag.of(ChatColor.stripColor(is.getItemMeta().getDisplayName()));
        if (tag == null) throw new RuntimeException();
        if (tags.getTags().contains(tag)) {
            tags.getTags().remove(tag);
            inventory.setItem(slot, ItemGenerator.getTag(ChatColor.AQUA + tag.toString(), Arrays.asList(ChatColor.RED + "当前状态: 禁用", ChatColor.YELLOW + "点击启用"), false));
        } else {
            tags.getTags().add(tag);
            inventory.setItem(slot, ItemGenerator.getTag(ChatColor.AQUA + tag.toString(), Arrays.asList(ChatColor.GREEN + "当前状态: 启用", ChatColor.YELLOW + "点击禁用"), true));
        }
        player.updateInventory();
    }

    private void callBack(Player player) {
        Bukkit.getScheduler().runTask(instance, () -> {
            prevMenu.refresh();
            player.openInventory(prevMenu.getInventory());
        });
    }
}
