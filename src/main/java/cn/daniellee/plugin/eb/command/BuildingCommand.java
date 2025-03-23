package cn.daniellee.plugin.eb.command;

import cn.daniellee.plugin.eb.ExcellentBuilding;
import cn.daniellee.plugin.eb.core.BuildingCore;
import cn.daniellee.plugin.eb.menu.BuildingMenu;
import cn.daniellee.plugin.eb.menu.ReviewMenu;
import cn.daniellee.plugin.eb.menu.filter.TagHandler;
import cn.daniellee.plugin.eb.menu.sort.SortRule;
import cn.daniellee.plugin.eb.model.Building;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BuildingCommand implements TabExecutor {

	public BuildingCommand() {
		Bukkit.getPluginCommand("excellentbuilding").setExecutor(this);
		Bukkit.getPluginCommand("excellentbuilding").setTabCompleter(this);
	}

	@Override
	public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
		// 玩家无参数使用则打开建筑菜单
		if (strings.length == 0 && commandSender instanceof Player) {
			Player player = (Player) commandSender;
			player.openInventory(BuildingMenu.generate(player, 1, TagHandler.NULL_TAG, SortRule.ID));
		} else if (strings.length != 0) {
			// 打开审核GUI
			if ((strings[0].equalsIgnoreCase("r") || strings[0].equalsIgnoreCase("review")) && commandSender instanceof Player && commandSender.hasPermission("building.command.review")) {
				Player player = (Player) commandSender;
				player.openInventory(ReviewMenu.generate(1));
			// 进行地标申请
			} else if ((strings[0].equalsIgnoreCase("s") || strings[0].equalsIgnoreCase("submit")) && strings.length > 1 && commandSender instanceof Player && commandSender.hasPermission("building.command.submit")) {
				Player player = (Player) commandSender;
				int maxLength = ExcellentBuilding.getInstance().getConfig().getInt("setting.name-max-length", 20);
				if (strings[1].length() <= maxLength) {
					if (BuildingCore.isPlayerNoPendingReview(player.getName())) {
						Building building = new Building();
						building.setId(ExcellentBuilding.getInstance().getStorage().getNextId());
						building.setName(strings[1]);
						building.setPlayer(player.getName());
						building.setServer(BuildingCore.serverName);
						Location location = player.getLocation();
						building.setLocation(location.getWorld().getName() + "," + location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + (int) location.getYaw() + "," + (int) location.getPitch());
						building.setCreateDate(System.currentTimeMillis());
						ExcellentBuilding.getInstance().getStorage().addBuilding(building);
						player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.apply-success", "&eSuccessful submission of excellent building applications.")).replace("&", "§"));
						// 向所有服务器发送消息
						BuildingCore.sendRefreshMessage(player);
					} else player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.has-pending", "&eYour last application has not been processed, please be patient.")).replace("&", "§"));
				} else player.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.invalid-name", "&eName length must be less than &b{length} &echaracters.").replace("{length}", Integer.toString(maxLength))).replace("&", "§"));
			// 重载插件
			} else if (strings[0].equalsIgnoreCase("reload") && commandSender.hasPermission("building.command.reload")) {
				ExcellentBuilding.getInstance().reloadConfig();
				if (ExcellentBuilding.getInstance().loadConfig()) {
					commandSender.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.reload-success", "&eReload has been successful.")).replace("&", "§"));
				}
				// 发送帮助
			} else sendHelp(commandSender);
		} else commandSender.sendMessage((ExcellentBuilding.getInstance().getPrefix() + ExcellentBuilding.getInstance().getConfig().getString("message.player-only", "&eThe console cannot open the GUI menu.")).replace("&", "§"));
		return true;
	}

	private void sendHelp(CommandSender commandSender) {
		commandSender.sendMessage(("&m&6---&m&a--------&3 : " + ExcellentBuilding.getInstance().getConfig().getString("prompt-prefix", "&6ExcellentBuilding") + "&3 : &m&a--------&m&6---").replace("&", "§"));

		String mainText =  ExcellentBuilding.getInstance().getConfig().getString("help.main", "&eOpen the building browsing GUI menu.").replace("&", "§");
		TextComponent mainHelp = new TextComponent("/eb  " + mainText);
		mainHelp.setColor(ChatColor.GRAY);
		mainHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/eb"));
		mainHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(mainHelp).color(ChatColor.BLUE).create()));
		commandSender.spigot().sendMessage(mainHelp);

		String reviewText =  ExcellentBuilding.getInstance().getConfig().getString("help.review", "&eOpen the building audit GUI menu.").replace("&", "§");
		TextComponent reviewHelp = new TextComponent("/eb review(r)  " + reviewText);
		reviewHelp.setColor(ChatColor.GRAY);
		reviewHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/eb review"));
		reviewHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(reviewText).color(ChatColor.BLUE).create()));
		commandSender.spigot().sendMessage(reviewHelp);

		String submitText =  ExcellentBuilding.getInstance().getConfig().getString("help.submit", "&eSubmit a building review application.").replace("&", "§");
		TextComponent submitHelp = new TextComponent("/eb submit(s) <name>  " + submitText);
		submitHelp.setColor(ChatColor.GRAY);
		submitHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/eb submit "));
		submitHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(submitText).color(ChatColor.BLUE).create()));
		commandSender.spigot().sendMessage(submitHelp);

		String reloadText = ExcellentBuilding.getInstance().getConfig().getString("help.reload", "Reload configuration.").replace("&", "§");
		TextComponent reloadHelp = new TextComponent("/eb reload  " + reloadText);
		reloadHelp.setColor(ChatColor.GRAY);
		reloadHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/eb reload"));
		reloadHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(reloadText).color(ChatColor.BLUE).create()));
		commandSender.spigot().sendMessage(reloadHelp);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> suggestion = new ArrayList<>();
		if (args.length <= 1) {
			if (sender.hasPermission("building.command.submit")) {
				suggestion.add("submit");
			}
			if (sender.hasPermission("building.command.review")) {
				suggestion.add("review");
			}
			if (sender.hasPermission("building.command.reload")) {
				suggestion.add("reload");
			}
		}
		return suggestion;
	}
}
