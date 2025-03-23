package cn.daniellee.plugin.eb;

import cn.daniellee.plugin.eb.command.BuildingCommand;
import cn.daniellee.plugin.eb.component.ItemGenerator;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.listener.BungeeListener;
import cn.daniellee.plugin.eb.listener.MenuListener;
import cn.daniellee.plugin.eb.listener.PlayerListener;
import cn.daniellee.plugin.eb.menu.filter.BuildingTag;
import cn.daniellee.plugin.eb.storage.MysqlStorage;
import cn.daniellee.plugin.eb.storage.Storage;
import cn.daniellee.plugin.eb.storage.StorageConverter;
import cn.daniellee.plugin.eb.storage.YamlStorage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ExcellentBuilding extends JavaPlugin {

    public static ExcellentBuilding instance;

    private String prefix;

    private Storage storage;

    private boolean bungeecord;

    @Override
    public void onEnable() {
        instance = this;

        if (loadConfig()) {
            getLogger().info(" ");
            getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> ExcellentBuilding Loaded <<<<<<<<<<<<<<<<<<<<<<<<");
            getLogger().info(">>>>> If you encounter any bugs, please contact author's QQ: 3556577839 <<<<<");
            getLogger().info(" ");

            Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
            Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

            new BuildingCommand();

            bungeecord = Bukkit.getServer().spigot().getConfig().getBoolean("settings.bungeecord", false);

            if (bungeecord) {
                this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
                this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", new BungeeListener());
            }

            BuildingCore.fetchServerName();
        }
    }

    public boolean loadConfig() {
        saveDefaultConfig();
        reloadConfig();
        getLogger().info("[ExcellentBuilding] Loading config...");
        if(!getDataFolder().exists()) getDataFolder().mkdirs();
        if (storage instanceof MysqlStorage) {
            ((MysqlStorage) storage).close();
        }
        storage = getConfig().getBoolean("storage.mysql.use", false) ? new MysqlStorage() : new YamlStorage();
        BuildingTag.clear();
        for (String tag : getConfig().getStringList("valid-tags")) {
            BuildingTag.internalCreate(tag);
        }
        if (storage.initialize()) {
            getLogger().info("[ExcellentBuilding]Storage initialized.");
        } else {
            getLogger().info(" ");
            getLogger().info("[ExcellentBuilding]Initializing data store failed, please edit the config and reload the plugin.".replace("&", "§"));
            getLogger().info(" ");
            Bukkit.getPluginManager().disablePlugin(this);
            return false;
        }
        if (getConfig().getBoolean("storage.mysql.use", false) && getConfig().getBoolean("storage.mysql.convert", false)) {
            Storage yamlStorage = new YamlStorage();
            if (yamlStorage.initialize()) {
                StorageConverter converter = new StorageConverter((MysqlStorage) storage, (YamlStorage) yamlStorage);
                converter.execute();
                getConfig().set("storage.mysql.convert", false);
                saveConfig();
                getLogger().info("[ExcellentBuilding]Successfully transferred Yaml data to Mysql.");
            } else {
                getLogger().info(" ");
                getLogger().info("[ExcellentBuilding]Yaml data store initialization failed, data conversion canceled.".replace("&", "§"));
                getLogger().info(" ");
            }
        }
        prefix = "&7[&6" + getConfig().getString("prompt-prefix", "ExcellentBuilding") + "&7] &3: &r";
        ItemGenerator.disableSkullLoad = getConfig().getBoolean("setting.disable-skull-load", false);
        return true;
    }

    @Override
    public void onDisable() {
        if (storage instanceof MysqlStorage) {
            ((MysqlStorage) storage).close();
        }

        if (bungeecord) {
            this.getServer().getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
            this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "BungeeCord");
        }

        getLogger().info(" ");
        getLogger().info(">>>>>>>>>>>>>>>>>>>>>>>> ExcellentBuilding Unloaded <<<<<<<<<<<<<<<<<<<<<<<<");
        getLogger().info(" ");
    }

    public static ExcellentBuilding getInstance() {
        return instance;
    }

    public String getPrefix() {
        return prefix;
    }

	public Storage getStorage() {
		return storage;
	}

    public boolean isBungeecord() {
        return bungeecord;
    }
}
