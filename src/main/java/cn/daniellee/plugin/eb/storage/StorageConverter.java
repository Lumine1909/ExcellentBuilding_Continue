package cn.daniellee.plugin.eb.storage;

import cn.daniellee.plugin.eb.model.Building;

public class StorageConverter {

    private MysqlStorage mysqlStorage;

    private YamlStorage yamlStorage;

    public StorageConverter(MysqlStorage mysqlStorage, YamlStorage yamlStorage) {
        this.mysqlStorage = mysqlStorage;
        this.yamlStorage = yamlStorage;
    }

    public void execute() {
        for (Building building : yamlStorage.getAllBuilding()) {
            building.setId(mysqlStorage.getNextId());
            mysqlStorage.addBuilding(building);
        }
    }

}
