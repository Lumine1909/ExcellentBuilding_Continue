package cn.daniellee.plugin.eb.menu;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.component.ItemGenerator;
import org.bukkit.inventory.ItemStack;

import java.text.Collator;
import java.util.Locale;

public class Common {

	public static final Collator COLLATOR;

	static {
		COLLATOR = Collator.getInstance(Locale.CHINESE);
		COLLATOR.setStrength(Collator.PRIMARY);
	}

	public static ItemStack getCorner() {
		return ItemGenerator.getItem(" ", null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.corner.material", "MAGENTA_STAINED_GLASS_PANE"),  ExcellentBuilding.getInstance().getConfig().getInt("menu.common.corner.durability", 0));
	}

	public static ItemStack getBorder() {
		return ItemGenerator.getItem(" ", null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.border.material", "BLUE_STAINED_GLASS_PANE"),  ExcellentBuilding.getInstance().getConfig().getInt("menu.common.border.durability", 0));
	}

}
