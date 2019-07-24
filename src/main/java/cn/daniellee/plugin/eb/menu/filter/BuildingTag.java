package cn.daniellee.plugin.eb.menu.filter;

import cn.daniellee.plugin.eb.component.ItemGenerator;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BuildingTag {

    public static final Map<String, BuildingTag> validTags = new HashMap<>();

    private final String name;

    public static BuildingTag of(String name) {
        return validTags.getOrDefault(name, null);
    }

    public static void internalCreate(String name) {
        validTags.put(name, new BuildingTag(name));
    }

    public static void clear() {
        validTags.clear();
    }

    public static void fillInventory(Inventory inventory, TagHandler tags) {
        if (tags.getTags().size() > 53) throw new RuntimeException();
        int index = 0;
        for (Map.Entry<String, BuildingTag> entry : validTags.entrySet()) {
            if (tags.getTags().contains(entry.getValue())) {
                inventory.setItem(index, ItemGenerator.getTag(ChatColor.AQUA + entry.getKey(), Arrays.asList(ChatColor.GREEN + "当前状态: 启用", ChatColor.YELLOW + "点击禁用"), true));
            } else {
                inventory.setItem(index, ItemGenerator.getTag(ChatColor.AQUA + entry.getKey(), Arrays.asList(ChatColor.RED + "当前状态: 禁用", ChatColor.YELLOW + "点击启用"), false));
            }
            index++;
        }
    }

    public BuildingTag(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
