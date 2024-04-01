package at.shorty.polar.addon.commands;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.data.LogCountData;
import at.shorty.polar.addon.data.LogEntry;
import at.shorty.polar.addon.util.TimeRange;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class PolarLogsCommand extends Command {

    private final PolarLogs polarLogs;
    private final String command;

    public PolarLogsCommand(PolarLogs polarLogs, String command) {
        super(command, "Polar Logs Command", "/" + command, new ArrayList<>());
        this.polarLogs = polarLogs;
        this.command = command;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.hasPermission("polarlogs.command")) {
            commandSender.sendMessage("§cYou do not have permission to execute this command!");
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!commandSender.hasPermission("polarlogs.command.reload")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                commandSender.sendMessage("§7Reloading config...");
                commandSender.sendMessage("§8Hint: Not respecting changes to the \"command\" config value! (Restart required)");
                polarLogs.reloadPluginConfig();
                commandSender.sendMessage("§aConfig reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!commandSender.hasPermission("polarlogs.command.info")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                try {
                    int count = polarLogs.getLogs().countLogs(polarLogs.getLogs().getContext());
                    commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
                    commandSender.sendMessage("§7Log info for context §b" + polarLogs.getLogs().getContext() + "§7:");
                    commandSender.sendMessage("§7- Count: §b" + count + " logs");
                } catch (SQLException e) {
                    commandSender.sendMessage("§cContext does (probably) not exist!");
                    commandSender.sendMessage("§cFailed to count logs (error in console)");
                    e.printStackTrace();
                }
                return true;
            } else if (args[0].equalsIgnoreCase("trange")) {
                if (!commandSender.hasPermission("polarlogs.command.logs")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
                commandSender.sendMessage("§7Time range valid inputs:");
                commandSender.sendMessage("§7- §btoday §7- today");
                commandSender.sendMessage("§7- §byesterday §7- yesterday");
                commandSender.sendMessage("§7- §b1h §7- last hour");
                commandSender.sendMessage("§7- §b1d §7- last 24 hours");
                commandSender.sendMessage("§7- §b1w §7- last week");
                commandSender.sendMessage("§7- §b1m §7- last month");
                commandSender.sendMessage("§7- §byyyy-MM-dd §7- specific date");
                commandSender.sendMessage("§7- §byyyy-mm-dd;yyyy-MM-dd §7- date range");
                commandSender.sendMessage("§7- §byyyy-mm §7- month");
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("webhooks")) {
                if (!commandSender.hasPermission("polarlogs.command.webhooks")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                if (args[1].equalsIgnoreCase("test")) {
                    commandSender.sendMessage("§7Testing webhook...");
                    polarLogs.testWebhook();
                    commandSender.sendMessage("§aWebhook sent!");
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!commandSender.hasPermission("polarlogs.command.info.context")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String context = args[1];
                if (!context.matches("^[a-zA-Z0-9_]+$")) {
                    commandSender.sendMessage("§cInvalid context name, must be [a-zA-Z0-9_]");
                    return true;
                }
                try {
                    int count = polarLogs.getLogs().countLogs(context);
                    commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
                    commandSender.sendMessage("§7Log info for context §b" + context + "§7:");
                    commandSender.sendMessage("§7- Count: §b" + count + " logs");
                } catch (SQLException e) {
                    commandSender.sendMessage("§cContext does (probably) not exist!");
                    commandSender.sendMessage("§cFailed to count logs (error in console)");
                    e.printStackTrace();
                }
                return true;
            }
        } else if (args.length >= 3 && args[0].toLowerCase().matches("^logs|log$")) {
            if (!commandSender.hasPermission("polarlogs.command.logs")) {
                commandSender.sendMessage("§cYou do not have permission to execute this command!");
                return true;
            }
            if (args[1].equalsIgnoreCase("info")) {
                if (!commandSender.hasPermission("polarlogs.command.logs.info.player")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String name = args[2];
                if (!name.matches("^[a-zA-Z0-9_]{1,16}(:.*)?$")) {
                    commandSender.sendMessage("§cInvalid player name, must be [a-zA-Z0-9_] length 1-16");
                    return true;
                }
                String context = polarLogs.getLogs().getContext();
                if (args.length == 4) {
                    if (!commandSender.hasPermission("polarlogs.command.logs.info.player.context")) {
                        commandSender.sendMessage("§cYou do not have permission to execute this command!");
                        return true;
                    }
                    if (!args[3].matches("^[a-zA-Z0-9_]+$")) {
                        commandSender.sendMessage("§cInvalid context name, must be [a-zA-Z0-9_]");
                        return true;
                    }
                    context = args[3];
                }
                sendPlayerInfo(commandSender, context, name);
                return true;
            } else if (args[1].equalsIgnoreCase("view")) {
                if (!commandSender.hasPermission("polarlogs.command.logs.view.player")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String name = args[2];
                if (!name.matches("^[a-zA-Z0-9_]{1,16}(:.*)?$")) {
                    commandSender.sendMessage("§cInvalid player name, must be [a-zA-Z0-9_] length 1-16");
                    return true;
                }
                String context = polarLogs.getLogs().getContext();
                if (args.length == 4) {
                    if (!commandSender.hasPermission("polarlogs.command.logs.view.player.context")) {
                        commandSender.sendMessage("§cYou do not have permission to execute this command!");
                        return true;
                    }
                    if (!args[3].matches("^[a-zA-Z0-9_]+$")) {
                        commandSender.sendMessage("§cInvalid context name, must be [a-zA-Z0-9_]");
                        return true;
                    }
                    context = args[3];
                }
                int page = 1;
                if (args.length == 5) {
                    try {
                        page = Integer.parseInt(args[4]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage("§cInvalid page number!");
                        return true;
                    }
                }
                sendPlayerLogs(commandSender, context, name, page, args);
                return true;
            }
        }
        commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
        commandSender.sendMessage("§7Version: §b" + polarLogs.getDescription().getVersion());
        commandSender.sendMessage("§7Server context: §b" + polarLogs.getLogs().getContext());
        commandSender.sendMessage("§7Available subcommands:");
        commandSender.sendMessage("§breload §7- Reload the config");
        commandSender.sendMessage("§bwebhooks test §7- Test the detection webhook");
        commandSender.sendMessage("§btrange §7- See help page for time range type");
        commandSender.sendMessage("§binfo §7- View log info");
        commandSender.sendMessage("§binfo <context> §7- View log info of a context");
        commandSender.sendMessage("§blogs info <name>[:<trange>] [<context>] §7- View info for a player");
        commandSender.sendMessage("§blogs view <name>[:<trange>] [<context>] [<page>] §7- View logs for a player");
        commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
        return true;
    }

    public void sendPlayerLogs(CommandSender sender, String context, String name, int page, String[] args) {
        int entriesPerPage = 10;
        try {
            TimeRange timeRange = null;
            if (name.contains(":")) {
                String[] split = name.split(":");
                name = split[0];
                String trange = split[1];
                try {
                    timeRange = TimeRange.parseFromString(trange);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
            }
            int offset = (page - 1) * entriesPerPage;
            int totalCount = polarLogs.getLogs().getLogCountData(context, name, timeRange).getTotalCount();
            int maxPage = (int) Math.ceil((double) totalCount / entriesPerPage);
            if (page > maxPage) {
                sender.sendMessage("§cInvalid page number! (max: " + maxPage + ")");
                return;
            }
            List<LogEntry> logEntries = polarLogs.getLogs().getLogEntries(context, name, entriesPerPage, offset, timeRange);
            sender.sendMessage("§7Player logs §b" + name + "§7@§b" + context + "§7:");
            if (timeRange != null)
                sender.sendMessage("§7Time range: §b" + timeRange.formatStart(polarLogs.getLogs().getTimestampFormat()) + " - " + timeRange.formatEnd(polarLogs.getLogs().getTimestampFormat()));
            Logs logs = polarLogs.getLogs();
            for (LogEntry logEntry : logEntries) {
                String messageTemplate;
                String hoverTemplate;
                switch (logEntry.type) {
                    case "mitigation":
                        messageTemplate = "§eM§r " + logs.getMitigationMessage();
                        hoverTemplate = logs.getMitigationHoverText();
                        break;
                    case "detection":
                        messageTemplate = "§cD§r " + logs.getDetectionMessage();
                        hoverTemplate = logs.getDetectionHoverText();
                        break;
                    case "cloud_detection":
                        messageTemplate = "§cCD§r " + logs.getCloudDetectionMessage();
                        hoverTemplate = logs.getCloudDetectionHoverText();
                        break;
                    case "punishment":
                        messageTemplate = "§4P§r " + logs.getPunishmentMessage();
                        hoverTemplate = logs.getPunishmentHoverText();
                        break;
                    default:
                        messageTemplate = "§cUnknown log type!";
                        hoverTemplate = "§cUnknown log type!";
                        break;
                }
                String message = messageTemplate
                        .replace("%PLAYER_NAME%", logEntry.playerName)
                        .replace("%PLAYER_UUID%", logEntry.playerUuid)
                        .replace("%PLAYER_LATENCY%", String.valueOf(logEntry.playerLatency))
                        .replace("%PLAYER_CLIENT_VERSION_NAME%", logEntry.getPlayerVersion())
                        .replace("%PLAYER_CLIENT_BRAND%", logEntry.getPlayerBrand())
                        .replace("%TIMESTAMP%", new SimpleDateFormat(logs.getTimestampFormat()).format(new Date(logEntry.time)))
                        .replace("%VL%", String.valueOf(logEntry.vl))
                        .replace("%CHECK_TYPE%", logEntry.checkType)
                        .replace("%CHECK_NAME%", logEntry.checkName)
                        .replace("%DETAILS%", logEntry.details)
                        .replace("%PUNISHMENT%", logEntry.punishmentType)
                        .replace("%REASON%", logEntry.punishmentReason);
                String hoverText = hoverTemplate
                        .replace("%PLAYER_NAME%", logEntry.playerName)
                        .replace("%PLAYER_UUID%", logEntry.playerUuid)
                        .replace("%PLAYER_LATENCY%", String.valueOf(logEntry.playerLatency))
                        .replace("%PLAYER_CLIENT_VERSION_NAME%", logEntry.getPlayerVersion())
                        .replace("%PLAYER_CLIENT_BRAND%", logEntry.getPlayerBrand())
                        .replace("%TIMESTAMP%", new SimpleDateFormat(logs.getTimestampFormat()).format(new Date(logEntry.time)))
                        .replace("%VL%", String.valueOf(logEntry.vl))
                        .replace("%CHECK_TYPE%", logEntry.checkType)
                        .replace("%CHECK_NAME%", logEntry.checkName)
                        .replace("%DETAILS%", logEntry.details)
                        .replace("%PUNISHMENT%", logEntry.punishmentType)
                        .replace("%REASON%", logEntry.punishmentReason);
                message = ChatColor.translateAlternateColorCodes('&', message);
                hoverText = ChatColor.translateAlternateColorCodes('&', hoverText);
                TextComponent textComponent = new TextComponent(message);
                textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse("{'text': '" + hoverText + "'}")));
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    player.spigot().sendMessage(textComponent);
                } else {
                    sender.sendMessage(textComponent.toLegacyText());
                }
            }
            TextComponent pageComponent = new TextComponent("§7Page §b" + page + "§7/§b" + maxPage);
            if (page > 1) {
                TextComponent previousComponent = new TextComponent(" §7[§b«§7] ");
                previousComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command + " logs view " + args[2] + " " + context + " " + (page - 1)));
                pageComponent.addExtra(previousComponent);
            } else {
                pageComponent.addExtra(" §7[§c«§7] ");
            }
            if (page < maxPage) {
                TextComponent nextComponent = new TextComponent(" §7[§b»§7] ");
                nextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command + " logs view " + args[2] + " " + context + " " + (page + 1)));
                pageComponent.addExtra(nextComponent);
            } else {
                pageComponent.addExtra(" §7[§c»§7] ");
            }
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.spigot().sendMessage(pageComponent);
            } else {
                sender.sendMessage(pageComponent.toLegacyText());
            }
            sender.sendMessage("§7Total entries: §b" + totalCount + " §7- Displaying §b" + logEntries.size() + "§7 entries");
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to get player logs (error in console)");
            sender.sendMessage("§cInvalid context?");
            e.printStackTrace();
        }
    }

    public void sendPlayerInfo(CommandSender sender, String context, String name) {
        try {
            TimeRange timeRange = null;
            if (name.contains(":")) {
                String[] split = name.split(":");
                name = split[0];
                String trange = split[1];
                try {
                    timeRange = TimeRange.parseFromString(trange);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
            }
            LogCountData logCountData = polarLogs.getLogs().getLogCountData(context, name, timeRange);
            if (logCountData == null) {
                sender.sendMessage("§cPlayer not found in logs!");
                return;
            }
            sender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
            if (timeRange != null)
                sender.sendMessage("§7Time range: §b" + timeRange.formatStart(polarLogs.getLogs().getTimestampFormat()) + " - " + timeRange.formatEnd(polarLogs.getLogs().getTimestampFormat()));
            sender.sendMessage("§7Player info §b" + name + "§7@§b" + context + "§7:");
            TextComponent mitigations = new TextComponent("§7- Mitigations: §bx" + logCountData.getMitigations().values().stream().mapToInt(Integer::intValue).sum() + (polarLogs.getLogs().getStore().isMitigation() ? "" : " §c§o(locally disabled)"));
            mitigations.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getMitigations().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            TextComponent detections = new TextComponent("§7- Detections: §bx" + logCountData.getDetections().values().stream().mapToInt(Integer::intValue).sum());
            detections.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getDetections().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            TextComponent cloudDetections = new TextComponent("§7- Cloud Detections: §bx" + logCountData.getCloudDetections().values().stream().mapToInt(Integer::intValue).sum());
            cloudDetections.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getCloudDetections().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            TextComponent punishments = new TextComponent("§7- Punishments: §bx" + logCountData.getPunishments().values().stream().mapToInt(Integer::intValue).sum());
            punishments.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getPunishments().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.spigot().sendMessage(mitigations);
                player.spigot().sendMessage(detections);
                player.spigot().sendMessage(cloudDetections);
                player.spigot().sendMessage(punishments);
            } else {
                sender.sendMessage(mitigations.toLegacyText());
                sender.sendMessage(detections.toLegacyText());
                sender.sendMessage(cloudDetections.toLegacyText());
                sender.sendMessage(punishments.toLegacyText());
            }
        } catch (SQLException e) {
            sender.sendMessage("§cFailed to get player info (error in console)");
            sender.sendMessage("§cInvalid context?");
            e.printStackTrace();
        }
    }
}
