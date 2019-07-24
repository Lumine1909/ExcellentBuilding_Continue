package cn.daniellee.plugin.eb.storage;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.model.Building;

import java.sql.*;

public class MysqlStorage extends Storage {

	private Connection connection;

	private String tablePrefix;

	@Override
	public boolean initialize() {
		// 初始化Mysql驱动
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while getting the Mysql database driver.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
			return false;
		}
		// 初始化连接
		String url = "jdbc:mysql://" + ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.host", "localhost") + ":" + ExcellentBuilding.getInstance().getConfig().getInt("storage.mysql.port", 3306) + "/" + ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.database", "minecraft") + "?" + ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.parameter", "characterEncoding=utf-8&useSSL=false&autoReconnect=true");
		try {
			connection = DriverManager.getConnection(url, ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.username", "username"), ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.password", "password"));
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]Mysql connection information is incorrect.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
			return false;
		}
		// 初始化数据表
		tablePrefix = ExcellentBuilding.getInstance().getConfig().getString("storage.mysql.table_perfix", "eb_");
		String sql = "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "building` (" +
				"`id` varchar(12) NOT NULL," +
				"`name` varchar(48) DEFAULT NULL," +
				"`player` varchar(24) DEFAULT NULL," +
				"`server` varchar(24) DEFAULT NULL," +
				"`location` varchar(48) DEFAULT NULL," +
				"`create_date` bigint(20) DEFAULT '0'," +
				"`reviewed` tinyint(1) DEFAULT '0'," +
				"`likes` int(11) DEFAULT '0'," +
				"`liker` text," +
				"`icon_material` varchar(24) DEFAULT NULL," +
				"`icon_durability` smallint(6) DEFAULT NULL," +
				"`introduction` text," +
				"PRIMARY KEY (`id`)," +
				"KEY `id_UNIQUE` (`id`)" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8;";
		PreparedStatement statement = null;
		try {
			statement = connection.prepareStatement(sql);
			statement.execute();
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while initializing the Mysql data table.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
			return false;
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
		refreshCache();
		return true;
	}

	@Override
	public void refreshCache() {
		allBuilding.clear();
		PreparedStatement statement = null;
		try {
			String sql = "select * from " + tablePrefix + "building";
			statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			while (resultSet.next()) {
				Building building = new Building();
				building.setId(Integer.toString(resultSet.getInt("id")));
				building.setName(resultSet.getString("name"));
				building.setPlayer(resultSet.getString("player"));
				building.setServer(resultSet.getString("server"));
				building.setLocation(resultSet.getString("location"));
				building.setCreateDate(resultSet.getLong("create_date"));
				building.setReviewed(resultSet.getBoolean("reviewed"));
				building.setLikes(resultSet.getInt("likes"));
				building.setLiker(resultSet.getString("liker"));
				building.setIconMaterial(resultSet.getString("icon_material"));
				building.setIconDurability(resultSet.getInt("icon_durability"));
				building.setIntroduction(resultSet.getString("introduction"));
				allBuilding.add(building);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while reading building data from Mysql.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	@Override
	public void addBuilding(Building building) {
		PreparedStatement statement = null;
		try {
			String sql = "insert into " + tablePrefix + "building (id,name,player,server,location,create_date,reviewed,likes,liker,icon_material,icon_durability,introduction)values(?,?,?,?,?,?,?,?,?,?,?,?)";
			statement = connection.prepareStatement(sql);
			statement.setString(1, building.getId());
			statement.setString(2, building.getName());
			statement.setString(3, building.getPlayer());
			statement.setString(4, building.getServer());
			statement.setString(5, building.getLocation());
			statement.setLong(6, building.getCreateDate());
			statement.setBoolean(7, building.isReviewed());
			statement.setInt(8, building.getLikes());
			statement.setString(9, building.getLiker());
			statement.setString(10, building.getIconMaterial());
			statement.setInt(11, building.getIconDurability());
			statement.setString(12, building.getIntroduction());
			statement.executeUpdate();
			allBuilding.add(building);
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while writing building data to Mysql.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	@Override
	public String getNextId() {
		PreparedStatement statement = null;
		try {
			String sql = "select max(cast(id as signed)) max_id from " + tablePrefix + "building";
			statement = connection.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				return Long.toString(resultSet.getLong("max_id") + 1);
			}
			return "1";
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while writing building data to Mysql.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
		return Integer.toString(Integer.parseInt(allBuilding.get(allBuilding.size() - 1).getId()) + 1);
	}

	@Override
	public void updateBuilding(String id, String column, Object val) {
		String value = (String) val;
		Building building = getBuildingById(id);
		if (building == null) return;
		PreparedStatement statement = null;
		try {
			String sql = "update " + tablePrefix + "building set " + column + " = ? where id = ?";
			statement = connection.prepareStatement(sql);
			switch (column) {
				case "name":
					building.setName(value);
					statement.setString(1, value);
					break;
				case "server":
					building.setServer(value);
					statement.setString(1, value);
					break;
				case "location":
					building.setLocation(value);
					statement.setString(1, value);
					break;
				case "reviewed":
					boolean reviewed = Boolean.parseBoolean(value);
					building.setReviewed(reviewed);
					statement.setBoolean(1, reviewed);
					break;
				case "likes":
					int likes = Integer.parseInt(value);
					building.setLikes(likes);
					statement.setInt(1, likes);
					break;
				case "liker":
					building.setLiker(value);
					statement.setString(1, value);
					break;
				case "icon_material":
					building.setIconMaterial(value);
					statement.setString(1, value);
					break;
				case "icon_durability":
					int iconDurability = Integer.parseInt(value);
					building.setIconDurability(iconDurability);
					statement.setInt(1, iconDurability);
					break;
				case "introduction":
					building.setIntroduction(value);
					statement.setString(1, value);
					break;
				default:
					return;
			}
			statement.setString(2, id);
			statement.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while writing building data to Mysql.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	@Override
	public void deleteBuilding(String id) {
		Building building = getBuildingById(id);
		if (building == null) return;
		PreparedStatement statement = null;
		try {
			String sql = "delete from " + tablePrefix + "building where id = ?";
			statement = connection.prepareStatement(sql);
			statement.setString(1, id);
			statement.executeUpdate();
			allBuilding.remove(building);
		} catch (SQLException e) {
			e.printStackTrace();
			ExcellentBuilding.getInstance().getLogger().info(" ");
			ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]An error occurred while writing building data to Mysql.".replace("&", "§"));
			ExcellentBuilding.getInstance().getLogger().info(" ");
		} finally {
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException ignored) { }
			}
		}
	}

	public void close() {
		try {
			connection.close();
		} catch (SQLException ignored) { }
	}
}
