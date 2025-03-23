package cn.daniellee.plugin.eb.menu;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.component.ItemGenerator;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.model.Building;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReviewMenu {

    public static Inventory generate(int page) {
        List<Building> buildings = BuildingCore.getUnreviewedBuildings();
        int size = buildings.size() > 45 ? 54 : 45;
        Inventory menu = Bukkit.createInventory(new ReviewMenuHolder(), size, ExcellentBuilding.getInstance().getConfig().getString("menu.review.title", "&6&lReview building").replace("&", "§"));

        List<String> lore = ExcellentBuilding.getInstance().getConfig().getStringList("menu.review.button.lore");
        List<String> operationLore = ExcellentBuilding.getInstance().getConfig().getStringList("menu.review.button.operation-lore");
        SimpleDateFormat format = new SimpleDateFormat(ExcellentBuilding.getInstance().getConfig().getString("menu.common.date-format", "yyyy-MM-dd"));
        for (int i = 0; i < 45; i++) {
            int targetIndex = i + 45 * (page - 1);
            if (targetIndex >= buildings.size()) break;
            Building building = buildings.get(targetIndex);
            List<String> targetLore = new ArrayList<>(lore);
            for (int j = 0; j < targetLore.size(); j++) {
                targetLore.set(j, targetLore.get(j).replace("{player}", building.getPlayer()).replace("{likes}", Integer.toString(building.getLikes())).replace("{createDate}", format.format(new Date(building.getCreateDate()))).replace("{empty}", building.getIntroduction() != null && !"".equals(building.getIntroduction()) ? "" : ExcellentBuilding.getInstance().getConfig().getString("menu.empty-intro", "Empty")));
            }
            if (building.getIntroduction() != null && !"".equals(building.getIntroduction())) {
                List<String> introduction = new Gson().fromJson(building.getIntroduction(), new TypeToken<List<String>>() {
                }.getType());
                for (int n = 0; n < introduction.size(); n++) {
                    introduction.set(n, "&r" + introduction.get(n));
                }
                targetLore.addAll(introduction);
            }
            targetLore.addAll(operationLore);
            if (building.getIconMaterial() != null && !"".equals(building.getIconMaterial())) {
                menu.setItem(i, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.building.button.name", "&bBuilding name: &d{name} &7({id})").replace("{name}", building.getName()).replace("{id}", building.getId()), targetLore, building.getIconMaterial(), building.getIconDurability()));
            } else {
                menu.setItem(i, ItemGenerator.getSkullItem(building.getPlayer(), ExcellentBuilding.getInstance().getConfig().getString("menu.building.button.name", "&bBuilding name: &d{name} &7({id})").replace("{name}", building.getName()).replace("{id}", building.getId()), targetLore));
            }
        }


        if (size == 54) {
            // 上一页按钮
            if (page > 1) {
                menu.setItem(45, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.common.back.name", "&eBack to {page}").replace("{page}", "[" + (page - 1) + "]"), null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.back.item.material", "RED_WOOL"), ExcellentBuilding.getInstance().getConfig().getInt("menu.common.back.item.durability", 0)));
            }
            // 下一页按钮
            if (45 * page < buildings.size()) {
                menu.setItem(53, ItemGenerator.getItem(ExcellentBuilding.getInstance().getConfig().getString("menu.common.next.name", "&eNext to {page}").replace("{page}", "[" + (page + 1) + "]"), null, ExcellentBuilding.getInstance().getConfig().getString("menu.common.next.item.material", "LIME_WOOL"), ExcellentBuilding.getInstance().getConfig().getInt("menu.common.next.item.durability", 0)));
            }
        }
        return menu;
    }

    public static class ReviewMenuHolder implements InventoryHolder {

        @Override
        public Inventory getInventory() {
            return Bukkit.createInventory(null, 54);
        }
    }
}