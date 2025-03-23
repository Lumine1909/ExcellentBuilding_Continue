package cn.daniellee.plugin.eb.storage;

import cn.daniellee.plugin.eb.model.Building;

import java.util.ArrayList;
import java.util.List;

public abstract class Storage {

    List<Building> allBuilding = new ArrayList<>();

    public List<Building> getAllBuilding() {
        return allBuilding;
    }

    public Building getBuildingById(String id) {
        return allBuilding.stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public abstract boolean initialize();

    public abstract void refreshCache();

    public abstract void addBuilding(Building building);

    public abstract String getNextId();

    public abstract void updateBuilding(String id, String column, Object value);

    public abstract void deleteBuilding(String id);
}
