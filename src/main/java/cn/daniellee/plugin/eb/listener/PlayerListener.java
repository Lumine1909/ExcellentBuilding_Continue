package cn.daniellee.plugin.eb.listener;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.model.Building;
import cn.daniellee.plugin.eb.model.Edit;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

public class PlayerListener implements Listener {

    /**
     * 在有一名玩家进服时获取服务器名
     * 在有登记传送信息时将其传送
     *
     * @param e
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if (!BuildingCore.fetchedName) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    BuildingCore.fetchServerName();
                }
            }.runTaskLater(ExcellentBuilding.getInstance(), 1);
        }
        Player player = e.getPlayer();
        String id = BuildingCore.teleportMap.get(player.getName());
        if (id != null) {
            Building building = ExcellentBuilding.getInstance().getStorage().getBuildingById(id);
            if (building != null) {
                BuildingCore.teleportMap.remove(player.getName());
                String[] location = building.getLocation().split(",");
                // 五次重试传送
                final int[] times = {0};
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        boolean success = player.teleport(new Location(Bukkit.getWorld(location[0]), Double.parseDouble(location[1]), Double.parseDouble(location[2]), Double.parseDouble(location[3]), Float.parseFloat(location[4]), Float.parseFloat(location[5])));
                        if (success) {
                            player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.visit-success", "&eSuccessfully teleport to the target building.")).replace("&", "§"));
                            cancel();
                        }
                        times[0]++;
                        if (times[0] > 4) {
                            player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.visit-failed", "&eTemporarily unable to transfer you to the target building.")).replace("&", "§"));
                            cancel();
                        }
                    }
                }.runTaskTimer(ExcellentBuilding.getInstance(), 0, 1000);
            }
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        Edit edit = BuildingCore.editOperation.get(player.getName());
        if (edit != null) {
            if ("Name".equals(edit.getOperation())) {
                e.setCancelled(true);
                BuildingCore.editOperation.remove(player.getName());
                if ("cancel".equalsIgnoreCase(e.getMessage())) {
                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.edit-canceled", "&eThe edit operation has been canceled.")).replace("&", "§"));
                } else {
                    int maxLength = ExcellentBuilding.getInstance().getConfig().getInt("setting.name-max-length", 20);
                    String name = e.getMessage();
                    if (name.length() <= maxLength) {
                        ExcellentBuilding.getInstance().getStorage().updateBuilding(edit.getId(), "name", name);
                        ExcellentBuilding.getInstance().getStorage().updateBuilding(edit.getId(), "reviewed", "false");
                        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.setname-success", "&eSuccessfully edit the building name, management will review it for you as soon as possible.")).replace("&", "§"));
                        BuildingCore.sendRefreshMessage(player);
                    } else
                        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.invalid-name", "&eName length must be less than &b{length} &echaracters.").replace("{length}", Integer.toString(maxLength))).replace("&", "§"));
                }
            } else if ("Intro".equals(edit.getOperation())) {
                e.setCancelled(true);
                if (e.getMessage().startsWith("add ") || e.getMessage().startsWith("ADD ")) {
                    int maxLine = ExcellentBuilding.getInstance().getConfig().getInt("setting.introduction-max-line", 10);
                    if (edit.getIntroduction().size() < maxLine) {
                        edit.getIntroduction().add(e.getMessage().substring(4));
                        showIntroduction(e.getPlayer(), edit.getIntroduction());
                    } else
                        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.reach-max-line", "&eMaximum number of rows has been reached and cannot be added.")).replace("&", "§"));
                } else if ((e.getMessage().startsWith("set ") || e.getMessage().startsWith("SET "))) {
                    String[] split = e.getMessage().split(" ");
                    if (split.length > 2 && split[1].matches("^\\d+$")) {
                        int line = Integer.parseInt(split[1]);
                        if (line > 0 && edit.getIntroduction().size() >= line) {
                            edit.getIntroduction().set(line - 1, StringUtils.join(Arrays.copyOfRange(split, 2, split.length), " "));
                            showIntroduction(e.getPlayer(), edit.getIntroduction());
                        } else
                            player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.incorrect-line", "&eIncorrect line number, please check and resend.")).replace("&", "§"));
                    } else
                        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.incorrect-format", "&eIncorrect format, please check and resend.")).replace("&", "§"));
                } else if (e.getMessage().startsWith("del ") || e.getMessage().startsWith("DEL ")) {
                    String[] split = e.getMessage().split(" ");
                    if (split.length > 1 && split[1].matches("^\\d+$")) {
                        int line = Integer.parseInt(split[1]);
                        if (line > 0 && edit.getIntroduction().size() >= line) {
                            edit.getIntroduction().remove(line - 1);
                            showIntroduction(e.getPlayer(), edit.getIntroduction());
                        } else
                            player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.incorrect-line", "&eIncorrect line number, please check and resend.")).replace("&", "§"));
                    } else
                        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.incorrect-format", "&eIncorrect format, please check and resend.")).replace("&", "§"));
                } else if ("ok".equalsIgnoreCase(e.getMessage())) {
                    BuildingCore.editOperation.remove(player.getName());
                    ExcellentBuilding.getInstance().getStorage().updateBuilding(edit.getId(), "introduction", new Gson().toJson(edit.getIntroduction()));
                    ExcellentBuilding.getInstance().getStorage().updateBuilding(edit.getId(), "reviewed", "false");
                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.setintro-success", "&eSuccessfully edit the building introduction, management will review it for you as soon as possible.")).replace("&", "§"));
                    BuildingCore.sendRefreshMessage(player);
                } else if ("cancel".equalsIgnoreCase(e.getMessage())) {
                    BuildingCore.editOperation.remove(player.getName());
                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.edit-canceled", "&eThe edit operation has been canceled.")).replace("&", "§"));
                } else
                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.incorrect-format", "&eIncorrect format, please check and resend.")).replace("&", "§"));
            }
        }
    }

    private void showIntroduction(Player player, List<String> introduction) {
        player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.current-intro", "&eEdited successfully, the current introduction is:")).replace("&", "§"));
        for (int i = 0; i < introduction.size(); i++)
            player.sendMessage(("&e" + (i + 1) + ". &r" + introduction.get(i)).replace("&", "§"));
        player.sendMessage(ExcellentBuilding.getInstance().getConfig().getString("message.intro-tips", "&e(&badd [content] &eto add a line, &bset [line] [content] &eto modify a line, &bdel [line] &eto delete a line, &bok &eto complete, &bcancel &eto cancel)").replace("&", "§"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        BuildingCore.editOperation.remove(player.getName());
    }

}
