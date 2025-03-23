package cn.daniellee.plugin.eb.storage;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.model.Building;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class YamlStorage extends Storage {

    private File buildingFile = new File(ExcellentBuilding.getInstance().getDataFolder(), "building.yml");

    private FileConfiguration buildingData = new YamlConfiguration();

    @Override
    public boolean initialize() {
        try {
            if (!buildingFile.exists()) buildingFile.createNewFile();
            buildingData.load(buildingFile);
        } catch (Exception e) {
            e.printStackTrace();
            ExcellentBuilding.getInstance().getLogger().info(" ");
            ExcellentBuilding.getInstance().getLogger().info("&5[ExcellentBuilding]An error occurred in building file load.".replace("&", "§"));
            ExcellentBuilding.getInstance().getLogger().info(" ");
            return false;
        }
        refreshCache();
        return true;
    }

    @Override
    public void refreshCache() {
        allBuilding.clear();
        Set<String> ids = buildingData.getKeys(false);
        for (String id : ids) {
            Building building = new Building();
            building.setId(id);
            building.setName(buildingData.getString(id + ".name", "&e&lUnnamed"));
            building.setPlayer(buildingData.getString(id + ".player", ""));
            building.setServer(buildingData.getString(id + ".server", ""));
            building.setLocation(buildingData.getString(id + ".location", "world,0,0,0,0,0"));
            building.setCreateDate(buildingData.getLong(id + ".createDate", System.currentTimeMillis()));
            building.setReviewed(buildingData.getBoolean(id + ".reviewed", false));
            building.setLikes(buildingData.getInt(id + ".likes", 0));
            building.setLiker(buildingData.getString(id + ".liker"));
            building.setIconMaterial(buildingData.getString(id + ".iconMaterial"));
            building.setIconDurability(buildingData.getInt(id + ".iconDurability", 0));
            building.setIntroduction(buildingData.getString(id + ".introduction"));
            building.setTags(TagHandler.deserializeTags((List<String>) buildingData.getList(id + ".tags", Collections.emptyList())));
            allBuilding.add(building);
        }
    }

    @Override
    public void addBuilding(Building building) {
        String id = getNextId();
        // 写配置
        buildingData.set(id + ".name", building.getName());
        buildingData.set(id + ".player", building.getPlayer());
        buildingData.set(id + ".server", building.getServer());
        buildingData.set(id + ".location", building.getLocation());
        buildingData.set(id + ".createDate", building.getCreateDate());
        buildingData.set(id + ".tags", building.getTags().serializeTags());
        saveBuildingData();
        allBuilding.add(building);
    }

    @Override
    public void updateBuilding(String id, String column, Object value) {
        Building building = getBuildingById(id);
        if (building == null) return;
        switch (column) {
            case "name":
                building.setName((String) value);
                buildingData.set(id + ".name", value);
                break;
            case "server":
                building.setServer((String) value);
                buildingData.set(id + ".server", value);
                break;
            case "location":
                building.setLocation((String) value);
                buildingData.set(id + ".location", value);
                break;
            case "reviewed":
                boolean reviewed = Boolean.parseBoolean((String) value);
                building.setReviewed(reviewed);
                buildingData.set(id + ".reviewed", reviewed);
                break;
            case "likes":
                int likes = Integer.parseInt((String) value);
                building.setLikes(likes);
                buildingData.set(id + ".likes", likes);
                break;
            case "liker":
                building.setLiker((String) value);
                buildingData.set(id + ".liker", value);
                break;
            case "icon_material":
                building.setIconMaterial((String) value);
                buildingData.set(id + ".iconMaterial", value);
                break;
            case "icon_durability":
                int iconDurability = Integer.parseInt((String) value);
                building.setIconDurability(iconDurability);
                buildingData.set(id + ".iconDurability", iconDurability);
                break;
            case "introduction":
                building.setIntroduction((String) value);
                buildingData.set(id + ".introduction", value);
                break;
            case "tags":
                building.setTags(TagHandler.deserializeTags((List<String>) value));
                buildingData.set(id + ".tags", value);
                break;
            default:
                return;
        }
        saveBuildingData();
    }

    @Override
    public void deleteBuilding(String id) {
        Building building = getBuildingById(id);
        if (building == null) return;
        allBuilding.remove(building);
        buildingData.set(id, null);
        saveBuildingData();
    }

    @Override
    public String getNextId() {
        String nextId;
        if (allBuilding.size() > 0) {
            nextId = Integer.toString(Integer.parseInt(allBuilding.get(allBuilding.size() - 1).getId()) + 1);
        } else nextId = "1";
        return nextId;
    }

    private void saveBuildingData() {
        try {
            buildingData.save(buildingFile);
        } catch (IOException e) {
            ExcellentBuilding.getInstance().getLogger().info(" ");
            ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred in player file save.".replace("&", "§"));
            ExcellentBuilding.getInstance().getLogger().info(" ");
            e.printStackTrace();
        }
    }

}