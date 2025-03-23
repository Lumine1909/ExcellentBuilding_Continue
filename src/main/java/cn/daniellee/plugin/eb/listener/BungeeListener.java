package cn.daniellee.plugin.eb.listener;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.core.BuildingCore;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;

public class BungeeListener implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String s, Player player, byte[] bytes) {
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subChannel = in.readUTF();
        if ("GetServer".equals(subChannel)) {
            String serverName = in.readUTF();
            BuildingCore.serverName = serverName;
            ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]Successfully fetched the server name: " + serverName);
        } else if ("GetServers".equals(subChannel)) {
            if (System.currentTimeMillis() - BuildingCore.latestServerListTime > 180000) {
                String serverList = in.readUTF();
                BuildingCore.serverList = Arrays.asList(serverList.split(", "));
                ExcellentBuilding.getInstance().getLogger().info("[ExcellentBuilding]Successfully fetched the server list: " + serverList);
                BuildingCore.latestServerListTime = System.currentTimeMillis();
            }
        } else if ("ExcellentBuilding".equals(subChannel)) {
            short length = in.readShort();
            byte[] msgBytes = new byte[length];
            in.readFully(msgBytes);
            DataInputStream msgIn = new DataInputStream(new ByteArrayInputStream(msgBytes));
            try {
                String msg = msgIn.readUTF();
                if ("Refresh".equals(msg)) {
                    // 刷新Storage缓存
                    ExcellentBuilding.getInstance().getStorage().refreshCache();
                } else if ("Teleport".equals(msg)) {
                    String playerName = msgIn.readUTF();
                    String buildingId = msgIn.readUTF();
                    BuildingCore.teleportMap.put(playerName, buildingId);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
