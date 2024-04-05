package at.shorty.polar.addon.commands;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.data.LogCountData;
import at.shorty.polar.addon.data.LogEntry;
import at.shorty.polar.addon.util.TimeRange;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
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
            } else if (args[0].equalsIgnoreCase("trange")) {
                if (!commandSender.hasPermission("polarlogs.command.view")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                if (!polarLogs.getLogs().isConnected()) {
                    commandSender.sendMessage("§cDatabase connection not established!");
                    return true;
                }
                commandSender.sendMessage("§bPolar Logs §8- §7Addon by §cShorty");
                commandSender.sendMessage("§7Time range valid inputs:");
                commandSender.sendMessage("§7- §btoday §7- today");
                commandSender.sendMessage("§7- §byesterday §7- yesterday");
                commandSender.sendMessage("§7- §b1h §7- last 60 minutes");
                commandSender.sendMessage("§7- §b1d §7- last 24 hours");
                commandSender.sendMessage("§7- §b1w §7- last 7 days");
                commandSender.sendMessage("§7- §b1m §7- last 30 days");
                commandSender.sendMessage("§7- §byyyy-MM-dd §7- specific date");
                commandSender.sendMessage("§7- §byyyy-mm-dd;yyyy-MM-dd §7- date range");
                commandSender.sendMessage("§7- §byyyy-mm §7- specific month");
                return true;
            } else if (args[0].equalsIgnoreCase("view")) {
                if (!commandSender.hasPermission("polarlogs.command.view")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " §8- §7Help\n" +
                        PolarLogs.prefix + "§7Server context: §b" + polarLogs.getLogs().getContext() + "\n" +
                        PolarLogs.prefix + "§7Usage: §bview <query> [<page>]\n" +
                        PolarLogs.prefix + "§7Player query: §cp:<player>[@<context>][:<time range>]\n" +
                        PolarLogs.prefix + "§7Context query: §cc:<context>[:<time range>]\n" +
                        PolarLogs.prefix + "§7See \"trange\" subcommand for valid time range inputs.";
                commandSender.sendMessage(helpMessage);
                TextComponent exampleComponent = new TextComponent(PolarLogs.prefix + "§7§oHover for example usage");
                String text = "§7See log history of player §bShorty §7in default context\n" +
                        "§8/" + command + " view p:Shorty\n" +
                        "\n" +
                        "§7See §b24 hour §7log history of player §bShorty §7in context §bglobal§7\n" +
                        "§8/" + command + " view p:Shorty@global:1d\n" +
                        "\n" +
                        "§7See §b24 hour §7log history of context §bglobal§7\n" +
                        "§8/" + command + " view c:global:1d";
                exampleComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse("{'text': '" + text + "'}")));
                if (commandSender instanceof Player) {
                    Player player = (Player) commandSender;
                    player.spigot().sendMessage(exampleComponent);
                } else {
                    commandSender.sendMessage(PolarLogs.prefix + "§7Example usage (Console format): \n"
                            + PolarLogs.prefix + text);
                }
                return true;
            }
        }
        if (args.length >= 2) {
            if (!commandSender.hasPermission("polarlogs.command.logs")) {
                commandSender.sendMessage("§cYou do not have permission to execute this command!");
                return true;
            }
            if (!polarLogs.getLogs().isConnected()) {
                commandSender.sendMessage("§cDatabase connection not established!");
                return true;
            }
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
                } else {
                    String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " §8- §7Help\n" +
                            PolarLogs.prefix + "§7Available subcommands for §b" + args[0] + "§7:\n" +
                            PolarLogs.prefix + "§btest §7- Test the webhook";
                    commandSender.sendMessage(helpMessage);
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!commandSender.hasPermission("polarlogs.command.info.player")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String name = args[1];
                if (!name.matches("^[a-zA-Z0-9_]{1,16}$")) {
                    commandSender.sendMessage("§cInvalid player name, must be [a-zA-Z0-9_] length 1-16");
                    return true;
                }
                sendPlayerInfo(commandSender, polarLogs.getLogs().getContext(), name);
                return true;
            } else if (args[0].equalsIgnoreCase("view")) {
                if (!commandSender.hasPermission("polarlogs.command.view")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                String query = args[1];
                if (query.length() < 2) {
                    commandSender.sendMessage("§cInvalid query, must start with p: or c:");
                    return true;
                }
                boolean isPlayerQuery = query.startsWith("p:");
                boolean isContextQuery = query.startsWith("c:");
                String name = args[1].substring(2);
                String contextFilter = null;
                String timeRangeFilter = null;
                if (name.replaceAll("^[a-zA-Z0-9_]+", "").startsWith("@") && isPlayerQuery) {
                    String[] split = name.split("@");
                    name = split[0];
                    if (split.length == 2)
                        contextFilter = split[1];
                    if (contextFilter != null && contextFilter.contains(":")) {
                        String[] split2 = contextFilter.split(":");
                        contextFilter = split2[0];
                        if (split2.length == 2)
                            timeRangeFilter = split2[1];
                    }
                } else if (name.replaceAll("^[a-zA-Z0-9_]+", "").startsWith(":")) {
                    String[] split = name.split(":");
                    name = split[0];
                    if (split.length == 2)
                        timeRangeFilter = split[1];
                    if (timeRangeFilter != null && timeRangeFilter.contains("@") && isPlayerQuery) {
                        String[] split2 = timeRangeFilter.split("@");
                        timeRangeFilter = split2[0];
                        if (split2.length == 2)
                            contextFilter = split2[1];
                    }
                }
                if (!name.matches("^[a-zA-Z0-9_]{1,16}(:.*)?$") && isPlayerQuery) {
                    commandSender.sendMessage("§cInvalid player name, must be [a-zA-Z0-9_] length 1-16");
                    return true;
                }
                if (!isPlayerQuery && !isContextQuery) {
                    commandSender.sendMessage("§cInvalid query, must start with p: or c:");
                    return true;
                }
                if (isPlayerQuery && !commandSender.hasPermission("polarlogs.command.view.player")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                if (isPlayerQuery && contextFilter != null && !commandSender.hasPermission("polarlogs.command.view.player.context")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                if (isPlayerQuery && contextFilter != null && !contextFilter.matches("^[a-zA-Z0-9_]+$")) {
                    commandSender.sendMessage("§cInvalid context name, must be [a-zA-Z0-9_]");
                    return true;
                }
                if (isContextQuery && !commandSender.hasPermission("polarlogs.command.view.context")) {
                    commandSender.sendMessage("§cYou do not have permission to execute this command!");
                    return true;
                }
                if (isContextQuery && !name.matches("^[a-zA-Z0-9_]+$")) {
                    commandSender.sendMessage("§cInvalid context name, must be [a-zA-Z0-9_]");
                    return true;
                }
                if (timeRangeFilter != null && !timeRangeFilter.matches("^[a-zA-Z0-9_]+$")) {
                    commandSender.sendMessage("§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return true;
                }
                String context = polarLogs.getLogs().getContext();
                if (isPlayerQuery && contextFilter != null) {
                    context = contextFilter;
                }
                if (isContextQuery) {
                    context = name;
                }
                int page = 1;
                if (args.length == 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        commandSender.sendMessage("§cInvalid page number!");
                        return true;
                    }
                }
                sendPlayerLogs(commandSender, context, isContextQuery ? null : name, timeRangeFilter, page, args);
                return true;
            }
        }
        String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " - Help\n" +
                PolarLogs.prefix + "§7Server context: §b" + polarLogs.getLogs().getContext() + "\n" +
                PolarLogs.prefix + "§7Available subcommands:\n" +
                PolarLogs.prefix + "§breload §7- Reload the config\n" +
                PolarLogs.prefix + "§bwebhooks §7- Webhook actions\n" +
                PolarLogs.prefix + "§btrange §7- See help page for time ranges\n" +
                PolarLogs.prefix + "§binfo <player> §7- View information about a player\n" +
                PolarLogs.prefix + "§bview §7- View logs";
        commandSender.sendMessage(helpMessage);
        return true;
    }

    public void sendPlayerLogs(CommandSender sender, String context, String name, String timeRangeFilter, int page, String[] args) {
        int entriesPerPage = 10;
        try {
            TimeRange timeRange = null;
            if (timeRangeFilter != null) {
                try {
                    timeRange = TimeRange.parseFromString(timeRangeFilter);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
            }
            int offset = (page - 1) * entriesPerPage;
            int totalCount = polarLogs.getLogs().getLogCountData(context, name, timeRange).getTotalCount();
            if (totalCount == 0) {
                sender.sendMessage("§cNo logs available.");
                return;
            }
            int maxPage = (int) Math.ceil((double) totalCount / entriesPerPage);
            if (page > maxPage) {
                sender.sendMessage("§cInvalid page number! (max: " + maxPage + ")");
                return;
            }
            List<LogEntry> logEntries = polarLogs.getLogs().getLogEntries(context, name, entriesPerPage, offset, timeRange);
            if (name != null) {
                sender.sendMessage("§7Player logs §b" + name + "§7@§b" + context + "§7:");
            } else {
                sender.sendMessage("§7Context logs §b" + context + "§7:");
            }
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
                previousComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command + " view " + args[1] + " " + (page - 1)));
                pageComponent.addExtra(previousComponent);
            } else {
                pageComponent.addExtra(" §7[§c«§7] ");
            }
            if (page < maxPage) {
                TextComponent nextComponent = new TextComponent(" §7[§b»§7] ");
                nextComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command + " view " + args[1] + " " + (page + 1)));
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
            sender.sendMessage("§cContext not found");
        }
    }

    public void sendPlayerInfo(CommandSender sender, String context, String name) {
        try {
            TimeRange timeRange = null;
            if (name.contains(":")) {
                String[] split = name.split(":");
                if (split.length != 2) {
                    sender.sendMessage("§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
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
            TextComponent detections = new TextComponent("§7- Detections: §bx" + logCountData.getDetections().values().stream().mapToInt(Integer::intValue).sum() + (polarLogs.getLogs().getStore().isDetection() ? "" : " §c§o(locally disabled)"));
            detections.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getDetections().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            TextComponent cloudDetections = new TextComponent("§7- Cloud Detections: §bx" + logCountData.getCloudDetections().values().stream().mapToInt(Integer::intValue).sum() + (polarLogs.getLogs().getStore().isCloudDetection() ? "" : " §c§o(locally disabled)"));
            cloudDetections.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(logCountData.getCloudDetections().entrySet().stream().map(entry -> "§7" + entry.getKey() + ": §bx" + entry.getValue()).collect(Collectors.joining("\n")))));
            TextComponent punishments = new TextComponent("§7- Punishments: §bx" + logCountData.getPunishments().values().stream().mapToInt(Integer::intValue).sum() + (polarLogs.getLogs().getStore().isPunishment() ? "" : " §c§o(locally disabled)"));
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
            sender.sendMessage("§cContext not found");
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        if (args.length == 1) {
            return Arrays.asList("reload", "info", "trange", "webhooks", "view");
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("webhooks")) {
                return Collections.singletonList("test");
            } else if (args[0].equalsIgnoreCase("info")) {
                return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("view")) {
                if (args[1].endsWith(":") && args[1].length() > 2) {
                    if (args[1].substring(2, args[1].length() - 1).contains(":"))
                        return Collections.singletonList(args[1]);
                    return Arrays.asList(args[1] + "today", args[1] + "yesterday", args[1] + "1h", args[1] + "1d", args[1] + "1w", args[1] + "1m");
                }
                if (args[1].startsWith("p:")) {
                    if (args[1].endsWith("@")) {
                        if (args[1].substring(2, args[1].length() - 1).contains("@")) {
                            return Collections.singletonList(args[1]);
                        }
                        return Collections.singletonList(args[1] + polarLogs.getLogs().getContext());
                    } else if (args[1].contains("@")) {
                        return Collections.singletonList(args[1]);
                    }
                    return Bukkit.getOnlinePlayers().stream().map(Player::getName).map(name -> "p:" + name).collect(Collectors.toList());
                } else if (args[1].startsWith("c:")) {
                    if (args[1].length() > 2 && args[1].substring(2, args[1].length() - 1).contains(":"))
                        return Collections.singletonList(args[1]);
                    return Collections.singletonList("c:" + polarLogs.getLogs().getContext());
                }
                if (args[1].isEmpty()) {
                    return Arrays.asList("p", "c");
                } else {
                    return Collections.emptyList();
                }
            }
        }
        return Collections.emptyList();
    }

}
