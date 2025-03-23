package cn.daniellee.plugin.eb.listener;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.menu.BuildingMenu;
import cn.daniellee.plugin.eb.menu.EditMenu;
import cn.daniellee.plugin.eb.menu.ReviewMenu;
import cn.daniellee.plugin.eb.menu.TagEditMenu;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.sort.SortRule;
import cn.daniellee.plugin.eb.model.Building;
import cn.daniellee.plugin.eb.model.Edit;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Inventory menu = e.getInventory();
        if (!ExcellentBuilding.getInstance().isBungeecord()) {
            return;
        }
        if (menu.getHolder() != null && (menu.getHolder() instanceof BuildingMenu || menu.getHolder() instanceof ReviewMenu.ReviewMenuHolder) && System.currentTimeMillis() - BuildingCore.latestServerListTime > 180000) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("GetServers");
            Player player = (Player) e.getPlayer();
            player.sendPluginMessage(ExcellentBuilding.getInstance(), "BungeeCord", out.toByteArray());
            BuildingCore.latestServerListTime = System.currentTimeMillis();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Inventory menu = e.getInventory();
        if (menu.getHolder() != null && menu.getHolder() instanceof EditMenu) {
            BuildingCore.editingBuilding.remove(e.getPlayer().getName());
        }
    }

    /**
     * 处理排队菜单和确认菜单的点击事件
     *
     * @param e 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory menu = e.getInventory();
        if (menu.getHolder() != null) {
            if (menu.getHolder() instanceof BuildingMenu) {
                BuildingMenu holder = (BuildingMenu) menu.getHolder();
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;
                String itemName = e.getCurrentItem().getItemMeta().getDisplayName();
                if (e.getRawSlot() == 45 || e.getRawSlot() == 53) {
                    Inventory playerMenu = BuildingMenu.generate(player, Integer.valueOf(itemName.substring(itemName.indexOf("[") + 1, itemName.indexOf("]"))), holder.getTags(), holder.getSortRule());
                    player.openInventory(playerMenu);
                } else if (e.getRawSlot() >= 48 && e.getRawSlot() <= 50) {
                    holder.handleChange(player, e.getRawSlot());
                } else {
                    String id = itemName.substring(itemName.lastIndexOf("(") + 1, itemName.lastIndexOf(")"));
                    Building building = BuildingCore.getBuildingById(id);
                    if (building != null) {
                        if (e.getClick().isShiftClick()) {
                            if (e.getClick().isLeftClick()) {
                                if (player.getName().equals(building.getPlayer()) || player.hasPermission("building.modify.force")) {
                                    BuildingCore.editingBuilding.put(player.getName(), id);
                                    player.openInventory(EditMenu.generate(id, building.getTags()));
                                }
                            }
                        } else {
                            if (e.getClick().isLeftClick()) {
                                player.closeInventory();
                                BuildingCore.teleportToBuilding(player, building);
                            } else if (e.getClick().isRightClick()) {
                                List<String> liker = building.getLiker() != null ? new ArrayList<>(Arrays.asList(building.getLiker().split(","))) : new ArrayList<>();
                                if (liker.contains(player.getName())) {
                                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.already-like", "&eYou have already praised this building.")).replace("&", "§"));
                                } else {
                                    liker.add(player.getName());

                                    ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "likes", Integer.toString(building.getLikes() + 1));
                                    ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "liker", StringUtils.join(liker, ","));
                                    player.closeInventory();
                                    if (!ExcellentBuilding.getInstance().getConfig().getBoolean("setting.disable-like-boardcast", false)) {
                                        BuildingCore.sendMessageToAll(player, (ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.boardcast.like-success", "&d{player} &ehas successfully liked the &c{owner}&e`s building &d{name}&e!").replace("{player}", player.getName()).replace("{owner}", building.getPlayer()).replace("{name}", building.getName())).replace("&", "§"));
                                    }
                                    // 向所有服务器发送消息
                                    BuildingCore.sendRefreshMessage(player);
                                }
                            }
                        }
                    }
                }
            } else if (menu.getHolder() instanceof ReviewMenu.ReviewMenuHolder) {
                e.setCancelled(true);
                Player player = (Player) e.getWhoClicked();
                if (!player.hasPermission("building.command.review")) return;
                if (e.getCurrentItem() == null || e.getCurrentItem().getItemMeta() == null) return;
                String itemName = e.getCurrentItem().getItemMeta().getDisplayName();
                if (e.getRawSlot() == 45 || e.getRawSlot() == 53) {
                    Inventory playerMenu = BuildingMenu.generate(player, Integer.valueOf(itemName.substring(itemName.indexOf("[") + 1, itemName.indexOf("]"))), TagHandler.NULL_TAG, SortRule.ID);
                    player.openInventory(playerMenu);
                } else {
                    String id = itemName.substring(itemName.indexOf("(") + 1);
                    id = id.substring(0, id.indexOf(")"));
                    Building building = BuildingCore.getBuildingById(id);
                    if (building != null) {
                        if (e.getClick().isShiftClick()) {
                            if (e.getClick().isLeftClick()) {
                                building.setReviewed(true);
                                ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "reviewed", "true");
                                BuildingCore.sendMessageToAll(player, (ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.boardcast.review-pass", "&c{owner}&e's building &d{name}&e has passed the excellent building review, go visit it!").replace("{owner}", building.getPlayer()).replace("{name}", building.getName())).replace("&", "§"));
                                // 重新打开以刷新
                                player.openInventory(ReviewMenu.generate(1));
                                // 向所有服务器发送消息
                                BuildingCore.sendRefreshMessage(player);
                            } else if (e.getClick().isRightClick()) {
                                ExcellentBuilding.getInstance().getStorage().deleteBuilding(id);
                                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.review-denied", "&eSuccessfully rejected the review of this building.")).replace("&", "§"));
                                // 重新打开以刷新
                                player.openInventory(ReviewMenu.generate(1));
                                // 向所有服务器发送消息
                                BuildingCore.sendRefreshMessage(player);
                            }
                        } else {
                            if (e.getClick().isLeftClick()) {
                                player.closeInventory();
                                BuildingCore.teleportToBuilding(player, building);
                            }
                        }
                    }
                }
            } else if (menu.getHolder() instanceof EditMenu) {
                e.setCancelled(true);
                // 防止取出物品
                if (e.isShiftClick()) return;
                Player player = (Player) e.getWhoClicked();
                String id = BuildingCore.editingBuilding.get(player.getName());
                if (id != null) {
                    Building building = BuildingCore.getBuildingById(id);
                    if (building != null) {
                        if (player.getName().equals(building.getPlayer()) || player.hasPermission("building.modify.force")) {
                            if (e.getRawSlot() == 1) {
                                BuildingCore.editOperation.put(player.getName(), new Edit(id, "Name"));
                                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.typein-chat", "&ePlease enter the new name into the chat box or enter &ccancel &eto cancel.")).replace("&", "§"));
                                player.closeInventory();
                            } else if (e.getRawSlot() == 2) {
                                Edit edit = new Edit(id, "Intro");
                                List<String> introduction = new ArrayList<>();
                                if (building.getIntroduction() != null && !"".equals(building.getIntroduction())) {
                                    introduction.addAll(new Gson().fromJson(building.getIntroduction(), new TypeToken<List<String>>() {
                                    }.getType()));
                                }
                                edit.setIntroduction(introduction);
                                BuildingCore.editOperation.put(player.getName(), edit);
                                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.intro-mode", "&eEnter the introduction edit mode, send &badd [content] &eto add a line, send &bset [line] [content] &eto modify a line, send &bdel [line] &eto delete a line, send &bok &eto complete, send &bcancel &eto cancel, the current introduction is:")).replace("&", "§"));
                                for (int i = 0; i < introduction.size(); i++)
                                    player.sendMessage(("&e" + (i + 1) + ". &r" + introduction.get(i)).replace("&", "§"));
                                player.closeInventory();
                            } else if (e.getRawSlot() == 3) {
                                Edit edit = BuildingCore.editOperation.get(player.getName());
                                if (edit == null || !"Location".equals(edit.getOperation())) {
                                    edit = new Edit(id, "Location");
                                    BuildingCore.editOperation.put(player.getName(), edit);
                                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.reclick-setloc", "&eClick again to confirm the update location.")).replace("&", "§"));
                                } else {
                                    BuildingCore.editOperation.remove(player.getName());
                                    ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "server", BuildingCore.serverName);
                                    Location location = player.getLocation();
                                    ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "location", location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + (int) location.getYaw() + "," + (int) location.getPitch());
                                    ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "reviewed", "false");
                                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.setloc-success", "&eSuccessfully updated building location, management will review for you as soon as possible.")).replace("&", "§"));
                                    player.closeInventory();
                                    BuildingCore.sendRefreshMessage(player);
                                }
                            } else if (e.getRawSlot() == 4) {
                                ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "icon_material", player.getItemInHand().getType() != Material.AIR ? player.getItemInHand().getType().toString() : null);
                                ExcellentBuilding.getInstance().getStorage().updateBuilding(id, "icon_durability", player.getItemInHand().getType() != Material.AIR ? String.valueOf(player.getItemInHand().getDurability()) : "0");
                                player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.seticon-success", "&eSuccessfully updated building icon.")).replace("&", "§"));
                                player.closeInventory();
                                BuildingCore.sendRefreshMessage(player);
                            } else if (e.getRawSlot() == 5) {
                                Edit edit = BuildingCore.editOperation.get(player.getName());
                                if (edit == null || !"Delete".equals(edit.getOperation())) {
                                    edit = new Edit(id, "Delete");
                                    BuildingCore.editOperation.put(player.getName(), edit);
                                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.reclick-delete", "&eClick again to confirm the deletion of the building.")).replace("&", "§"));
                                } else {
                                    BuildingCore.editOperation.remove(player.getName());
                                    ExcellentBuilding.getInstance().getStorage().deleteBuilding(id);
                                    player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.delete-success", "&eSuccessfully deleted building.")).replace("&", "§"));
                                    player.closeInventory();
                                    BuildingCore.sendRefreshMessage(player);
                                }
                            } else if (e.getRawSlot() == 6) {
                                player.openInventory(new TagEditMenu(((EditMenu) menu.getHolder()).getTags(), (EditMenu) menu.getHolder()).getInventory());
                            }
                        }
                    }
                }
            } else if (menu.getHolder() instanceof TagEditMenu) {
                TagEditMenu menu1 = (TagEditMenu) menu.getHolder();
                if (e.getRawSlot() > 53 || e.getRawSlot() < 0) {
                    return;
                }
                menu1.handleClick((Player) e.getWhoClicked(), e.getRawSlot());
                e.setCancelled(true);
            }
        }
    }

}
