package cn.daniellee.plugin.eb.core;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.sort.SortRule;
import cn.daniellee.plugin.eb.model.Building;
import cn.daniellee.plugin.eb.model.Edit;
import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BuildingCore {

    public static String serverName = "SenCraft";

    public static List<String> serverList;

    public static long latestServerListTime;

    public static boolean fetchedName = false;

    public static Map<String, String> teleportMap = new HashMap<>();

    public static Map<String, String> editingBuilding = new HashMap<>();

    public static Map<String, Edit> editOperation = new HashMap<>();

    public static void fetchServerName() {
        if (ExcellentBuilding.getInstance().isBungeecord()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServer");
            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
            if (player != null) {
                player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
                BuildingCore.fetchedName = true;
            }
        }
    }

    public static List<Building> getAndSortReviewedBuildings(TagHandler filter, SortRule sortRule) {
        List<Building> reviewedBuildings = ExcellentBuilding.getInstance().getStorage().getAllBuilding().stream().filter(Building::isReviewed).collect(Collectors.toList());
        return reviewedBuildings.stream()
            .filter(filter::filterBuilding).sorted(sortRule::compare)
            .collect(Collectors.toList());
    }

    public static List<Building> getUnreviewedBuildings() {
        return ExcellentBuilding.getInstance().getStorage().getAllBuilding().stream().filter(item -> !item.isReviewed()).collect(Collectors.toList());
    }

    public static boolean isPlayerNoPendingReview(String name) {
        return ExcellentBuilding.getInstance().getStorage().getAllBuilding().stream().noneMatch(item -> !item.isReviewed() && item.getPlayer().equals(name));
    }

    public static Building getBuildingById(String id) {
        return ExcellentBuilding.getInstance().getStorage().getAllBuilding().stream().filter(item -> item.getId().equals(id)).findFirst().orElse(null);
    }

    public static void sendRefreshMessage(Player player) {
        if (ExcellentBuilding.getInstance().isBungeecord()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Forward");
            out.writeUTF("ALL");
            out.writeUTF("ExcellentBuilding");
            ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
            DataOutputStream msgOut = new DataOutputStream(msgBytes);
            try {
                msgOut.writeUTF("Refresh");
            } catch (IOException ignored) {
            }
            out.writeShort(msgBytes.toByteArray().length);
            out.write(msgBytes.toByteArray());
            player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
        }
    }

    public static void sendMessageToAll(Player player, String message) {
        if (ExcellentBuilding.getInstance().isBungeecord()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Message");
            out.writeUTF("ALL");
            out.writeUTF(message);
            player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
        } else Bukkit.broadcastMessage(message);
    }

    public static void teleportToBuilding(Player player, Building building) {
        if (BuildingCore.serverName.equals(building.getServer())) {
            String[] location = building.getLocation().split(",");
            if (player.teleport(new Location(Bukkit.getWorld(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])))) {
                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.visit-success", "&eSuccessfully teleport to the target building.")).replace("&", "§"));
            } else {
                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.visit-failed", "&eTemporarily unable to transfer you to the target building.")).replace("&", "§"));
            }
        } else if (ExcellentBuilding.getInstance().isBungeecord()) {
            if (BuildingCore.serverList.contains(building.getServer())) {
                // 向目标服务器发送消息
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeUTF("Forward");
                out.writeUTF(building.getServer());
                out.writeUTF("ExcellentBuilding");
                ByteArrayOutputStream msgBytes = new ByteArrayOutputStream();
                DataOutputStream msgOut = new DataOutputStream(msgBytes);
                try {
                    msgOut.writeUTF("Teleport");
                    msgOut.writeUTF(player.getName());
                    msgOut.writeUTF(building.getId());
                } catch (IOException ignored) {
                }
                out.writeShort(msgBytes.toByteArray().length);
                out.write(msgBytes.toByteArray());
                player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
                // 将玩家传送到目标服务器
                out = ByteStreams.newDataOutput();
                out.writeUTF("Connect");
                out.writeUTF(building.getServer());
                player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
            } else
                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.server-invalid", "&eThe target server is not running.")).replace("&", "§"));
        }
    }
}
