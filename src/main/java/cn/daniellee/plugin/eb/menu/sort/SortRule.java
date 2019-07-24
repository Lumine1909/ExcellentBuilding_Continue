package cn.daniellee.plugin.eb.menu.sort;

import cn.daniellee.plugin.eb.model.Building;
import org.bukkit.ChatColor;

import java.util.function.BiFunction;

import static cn.daniellee.plugin.eb.menu.Common.COLLATOR;

public enum SortRule {

    ID((a, b) -> a.getId().compareTo(b.getId()), "建筑id"),
    ATOZ((a, b) -> COLLATOR.compare(ChatColor.stripColor(a.getName()), ChatColor.stripColor(b.getName())), "名称A-Z (忽略大小写)"),
    ZTOA((a, b) -> COLLATOR.reversed().compare(ChatColor.stripColor(a.getName()), ChatColor.stripColor(b.getName())), "名称Z-A (忽略大小写)"),
    LATESTSUBMIT((a, b) -> a.getCreateDate() > b.getCreateDate() ? -1 : 1, "创建时间最晚"),
    OLDESTSUBMIT((a, b) -> a.getCreateDate() > b.getCreateDate() ? 1 : -1, "创建时间最早"),
    MOSTLIKE((a, b) -> Integer.compare(b.getLikes(), a.getLikes()), "点赞最多");

    private BiFunction<Building, Building, Integer> comprator;
    private final String name;

    SortRule(BiFunction<Building, Building, Integer> comprator, String name) {
        this.comprator = comprator;
        this.name = name;
    }

    public int compare(Building a, Building b) {
        return comprator.apply(a, b);
    }

    public String getName() {
        return name;
    }
}
