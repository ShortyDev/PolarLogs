package at.shorty.polar.addon.commands;

import at.shorty.polar.addon.PolarLogs;
import at.shorty.polar.addon.config.Logs;
import at.shorty.polar.addon.data.LogCountData;
import at.shorty.polar.addon.data.LogEntry;
import at.shorty.polar.addon.data.LogQuery;
import at.shorty.polar.addon.util.TimeRange;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import top.polar.api.command.Subcommand;
import top.polar.api.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PolarLogsCommand extends Subcommand {

    private final PolarLogs polarLogs;
    private final String command;

    public PolarLogsCommand(PolarLogs polarLogs, String command) {
        super(command, false, "polarlogs.command");
        this.polarLogs = polarLogs;
        this.command = command;
    }

    @Override
    public void execute(User user, String s, String[] args) {
        if (!hasPermission(user, "polarlogs.command")) {
            sendMessage(user, "§cYou do not have permission to execute this command!");
            return;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (!hasPermission(user, "polarlogs.command.reload")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                sendMessage(user, "§7Reloading config...");
                sendMessage(user, "§8Hint: Not respecting changes to the \"command\" config value! (Restart required)");
                polarLogs.reloadPluginConfig();
                sendMessage(user, "§aConfig reloaded!");
                return;
            } else if (args[0].equalsIgnoreCase("trange")) {
                if (!hasPermission(user, "polarlogs.command.view")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (!polarLogs.getLogs().isConnected()) {
                    sendMessage(user, "§cDatabase connection not established!");
                    return;
                }
                sendMessage(user, "§bPolar Logs §8- §7Addon by §cShorty");
                sendMessage(user, "§7Time range valid inputs:");
                sendMessage(user, "§7- §btoday §7- today");
                sendMessage(user, "§7- §byesterday §7- yesterday");
                sendMessage(user, "§7- §b1h §7- last 60 minutes");
                sendMessage(user, "§7- §b1d §7- last 24 hours");
                sendMessage(user, "§7- §b1w §7- last 7 days");
                sendMessage(user, "§7- §b1m §7- last 30 days");
                sendMessage(user, "§7- §byyyy-MM-dd §7- specific date");
                sendMessage(user, "§7- §byyyy-mm-dd;yyyy-MM-dd §7- date range");
                sendMessage(user, "§7- §byyyy-mm §7- specific month");
                return;
            } else if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("export")) {
                if (!hasPermission(user, "polarlogs.command." + args[0].toLowerCase())) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " §8- §7Help\n" +
                        PolarLogs.prefix + "§7Server context: §b" + polarLogs.getLogs().getContext() + "\n" +
                        PolarLogs.prefix + "§7Usage: §b" + args[0].toLowerCase() + " <query> " + (args[0].equalsIgnoreCase("view") ? "[<page>]" : "[includeDetails]") + "\n" +
                        PolarLogs.prefix + "§7Player query: §cp:<player>[@<context>][:<time range>]\n" +
                        PolarLogs.prefix + "§7Context query: §cc:<context>[:<time range>]\n" +
                        PolarLogs.prefix + "§7See \"trange\" subcommand for valid time range inputs.";
                sendMessage(user, helpMessage);
                TextComponent exampleComponent = new TextComponent(PolarLogs.prefix + "§7§oHover for example usage");
                String text = "§7See log history of player §bShorty §7in default context\n" +
                        "§8/" + command + " view p:Shorty\n" +
                        "\n" +
                        "§7See §b24 hour §7log history of player §bShorty §7in context §bglobal§7\n" +
                        "§8/" + command + " view p:Shorty@global:1d\n" +
                        "\n" +
                        "§7See §b24 hour §7log history of context §bglobal§7\n" +
                        "§8/" + command + " view c:global:1d";
                if (args[0].equalsIgnoreCase("export"))
                    text = text
                            .replace("See", "Export")
                            .replace("view", "export");
                exampleComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentSerializer.parse("{'text': '" + text + "'}")));
                if (user instanceof Player) {
                    Player player = (Player) user;
                    player.spigot().sendMessage(exampleComponent);
                } else {
                    sendMessage(user, PolarLogs.prefix + "§7Example usage (Console format): \n"
                            + PolarLogs.prefix + text);
                }
                return;
            }
        }
        if (args.length >= 2) {
            if (!hasPermission(user, "polarlogs.command.logs")) {
                sendMessage(user, "§cYou do not have permission to execute this command!");
                return;
            }
            if (!polarLogs.getLogs().isConnected() && !args[0].equalsIgnoreCase("webhooks")) {
                sendMessage(user, "§cDatabase connection not established!");
                return;
            }
            if (args[0].equalsIgnoreCase("webhooks")) {
                if (!hasPermission(user, "polarlogs.command.webhooks")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (args[1].equalsIgnoreCase("test")) {
                    sendMessage(user, "§7Testing webhook...");
                    polarLogs.testWebhook();
                    sendMessage(user, "§aWebhook sent!");
                    return;
                } else {
                    String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " §8- §7Help\n" +
                            PolarLogs.prefix + "§7Available subcommands for §b" + args[0] + "§7:\n" +
                            PolarLogs.prefix + "§btest §7- Test the webhook";
                    sendMessage(user, helpMessage);
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (!hasPermission(user, "polarlogs.command.info.player")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String name = args[1];
                if (!name.matches("^[a-zA-Z0-9_]{1,16}$")) {
                    sendMessage(user, "§cInvalid player name, must be [a-zA-Z0-9_] length 1-16");
                    return;
                }
                sendPlayerInfo(user, polarLogs.getLogs().getContext(), name);
                return;
            } else if (args[0].equalsIgnoreCase("view")) {
                if (!hasPermission(user, "polarlogs.command.view")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String query = args[1];
                if (query.length() < 2) {
                    sendMessage(user, "§cInvalid query, must start with p: or c:");
                    return;
                }
                LogQuery logQuery;
                try {
                    logQuery = LogQuery.parseFrom(query);
                } catch (IllegalArgumentException e) {
                    sendMessage(user, "§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
                if (logQuery.context == null && logQuery.player == null) {
                    sendMessage(user, "§cInvalid query. Make sure any player or context names provided are valid.");
                    return;
                }
                boolean isPlayerQuery = logQuery.player != null;
                boolean isContextQuery = logQuery.player == null;
                String name = isPlayerQuery ? logQuery.player : logQuery.context;
                String contextFilter = logQuery.context;
                TimeRange timeRangeFilter = logQuery.timeRange;
                if (isPlayerQuery && !hasPermission(user, "polarlogs.command.view.player")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (isPlayerQuery && contextFilter != null && !hasPermission(user, "polarlogs.command.view.player.context")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (isContextQuery && !hasPermission(user, "polarlogs.command.view.context")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String context = polarLogs.getLogs().getContext();
                if (isPlayerQuery && contextFilter != null) {
                    context = contextFilter;
                }
                int page = 1;
                if (args.length == 3) {
                    try {
                        page = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sendMessage(user, "§cInvalid page number!");
                        return;
                    }
                }
                sendPlayerLogs(user, context, isContextQuery ? null : name, timeRangeFilter, page, args);
                return;
            } else if (args[0].equalsIgnoreCase("export")) {
                if (!hasPermission(user, "polarlogs.command.export")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String query = args[1];
                if (query.length() < 2) {
                    sendMessage(user, "§cInvalid query, must start with p: or c:");
                    return;
                }
                LogQuery logQuery;
                try {
                    logQuery = LogQuery.parseFrom(query);
                } catch (IllegalArgumentException e) {
                    sendMessage(user, "§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                    return;
                }
                if (logQuery.context == null && logQuery.player == null) {
                    sendMessage(user, "§cInvalid query. Make sure any player or context names provided are valid.");
                    return;
                }
                boolean isPlayerQuery = logQuery.player != null;
                boolean isContextQuery = logQuery.player == null;
                String name = isPlayerQuery ? logQuery.player : logQuery.context;
                String contextFilter = logQuery.context;
                TimeRange timeRangeFilter = logQuery.timeRange;
                if (isPlayerQuery && !hasPermission(user, "polarlogs.command.export.player")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (isPlayerQuery && contextFilter != null && !hasPermission(user, "polarlogs.command.export.player.context")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                if (isContextQuery && !hasPermission(user, "polarlogs.command.export.context")) {
                    sendMessage(user, "§cYou do not have permission to execute this command!");
                    return;
                }
                String context = polarLogs.getLogs().getContext();
                if (isPlayerQuery && contextFilter != null) {
                    context = contextFilter;
                }
                boolean includeDetails = args.length > 2 && args[2].equalsIgnoreCase("includeDetails");
                exportPlayerLogs(user, context, isContextQuery ? null : name, timeRangeFilter, includeDetails);
                return;
            }
        }
        String helpMessage = PolarLogs.prefix + "§7v" + polarLogs.getDescription().getVersion() + " - Help\n" +
                PolarLogs.prefix + "§7Server context: §b" + polarLogs.getLogs().getContext() + "\n" +
                PolarLogs.prefix + "§7Available subcommands:\n" +
                PolarLogs.prefix + "§breload §7- Reload the config\n" +
                PolarLogs.prefix + "§bwebhooks §7- Webhook actions\n" +
                PolarLogs.prefix + "§btrange §7- See help page for time ranges\n" +
                PolarLogs.prefix + "§binfo <player> §7- View information about a player\n" +
                PolarLogs.prefix + "§bview §7- View logs\n" +
                PolarLogs.prefix + "§bexport §7- Export logs";
        sendMessage(user, helpMessage);
    }

    private void exportPlayerLogs(User sender, String context, String name, TimeRange timeRange, boolean includeDetails) {
        long exportTime = System.currentTimeMillis();
        String humanExportTime = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date(exportTime));
        PolarLogs.getSpecialUtilityJustForFoliaSpecialNeeds().runAsyncNow(() -> {
            try {
                sendMessage(sender, PolarLogs.prefix + "§7Exporting logs... (async)");
                sendMessage(sender, PolarLogs.prefix + "§7Hard limit: 1000 entries");
                List<LogEntry> logEntries = polarLogs.getLogs().getLogEntries(context, name, 1000, 0, timeRange);
                boolean isPlayerExport = name != null;
                SimpleDateFormat timestampFormat = new SimpleDateFormat(polarLogs.getLogs().getTimestampFormat());
                String fileName = (isPlayerExport ? name : "ctx_" + context) + "_" + humanExportTime + ".txt";
                String fileContent = logEntries.stream().map(logEntry -> logEntry.getPlayerName() + ";" +
                        logEntry.getPlayerUuid() + ";" +
                        logEntry.getPlayerLatency() + "ms;" +
                        logEntry.getPlayerVersion() + ";" +
                        logEntry.getPlayerBrand() + ";" +
                        timestampFormat.format(new Date(logEntry.getTime())) + ";" +
                        logEntry.getVl() + "VL;" +
                        logEntry.getCheckType() + ";" +
                        logEntry.getCheckName() + ";" +
                        (includeDetails ? logEntry.getDetails().replace("\n", "+") + ";" : "") +
                        logEntry.getPunishmentType() + ";" +
                        logEntry.getPunishmentReason()).collect(Collectors.joining("\n"));
                File exportFolder = new File(polarLogs.getDataFolder(), "exports");
                if (!exportFolder.exists()) {
                    exportFolder.mkdirs();
                }
                File exportFile = new File(exportFolder, fileName);
                try {
                    Files.write(exportFile.toPath(), fileContent.getBytes());
                    sendMessage(sender, PolarLogs.prefix + "§7Exported §b" + logEntries.size() + " log entries §7to file §e" + polarLogs.getDataFolder().getPath() + "/exports/" + fileName + " §asuccessfully§7.");
                } catch (IOException e) {
                    sendMessage(sender, PolarLogs.prefix + "§cFailed to export logs to file! (details in console)");
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                sendMessage(sender, "§cContext not found");
            }
        });
    }

    public void sendPlayerLogs(User sender, String context, String name, TimeRange timeRange, int page, String[] args) {
        PolarLogs.getSpecialUtilityJustForFoliaSpecialNeeds().runAsyncNow(() -> {
           int entriesPerPage = 10;
            try {
                int offset = (page - 1) * entriesPerPage;
                LogCountData logCountData = polarLogs.getLogs().getLogCountData(context, name, timeRange);
                if (logCountData == null) {
                    sendMessage(sender, "§cPlayer not found in logs or database connection not established!");
                    return;
                }
                int totalCount = logCountData.getTotalCount();
                if (totalCount == 0) {
                    sendMessage(sender, "§cNo logs available.");
                    return;
                }
                int maxPage = (int) Math.ceil((double) totalCount / entriesPerPage);
                if (page > maxPage) {
                    sendMessage(sender, "§cInvalid page number! (max: " + maxPage + ")");
                    return;
                }
                List<LogEntry> logEntries = polarLogs.getLogs().getLogEntries(context, name, entriesPerPage, offset, timeRange);
                if (name != null) {
                    sendMessage(sender, "§7Player logs §b" + name + "§7@§b" + context + "§7:");
                } else {
                    sendMessage(sender, "§7Context logs §b" + context + "§7:");
                }
                if (timeRange != null)
                    sendMessage(sender, "§7Time range: §b" + timeRange.formatStart(polarLogs.getLogs().getTimestampFormat()) + " - " + timeRange.formatEnd(polarLogs.getLogs().getTimestampFormat()));
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
                    String logEntryDetails = logEntry.details.replaceAll("<.*>", "");
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
                            .replace("%DETAILS%", logEntryDetails)
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
                            .replace("%DETAILS%", logEntryDetails)
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
                        sendMessage(sender, textComponent.toLegacyText());
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
                    sendMessage(sender, pageComponent.toLegacyText());
                }
                sendMessage(sender, "§7Total entries: §b" + totalCount + " §7- Displaying §b" + logEntries.size() + "§7 entries");
            } catch (SQLException e) {
                sendMessage(sender, "§cContext not found");
            }
        });
    }

    public void sendPlayerInfo(User sender, String context, String name) {
        PolarLogs.getSpecialUtilityJustForFoliaSpecialNeeds().runAsyncNow(() -> {
            String name2 = name;
            try {
                TimeRange timeRange = null;
                if (name2.contains(":")) {
                    String[] split = name2.split(":");
                    if (split.length != 2) {
                        sendMessage(sender, "§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                        return;
                    }
                    name2 = split[0];
                    String trange = split[1];
                    try {
                        timeRange = TimeRange.parseFromString(trange);
                    } catch (IllegalArgumentException e) {
                        sendMessage(sender, "§cInvalid time range! See \"trange\" subcommand for valid inputs.");
                        return;
                    }
                }
                LogCountData logCountData = polarLogs.getLogs().getLogCountData(context, name2, timeRange);
                if (logCountData == null) {
                    sendMessage(sender, "§cPlayer not found in logs or database connection not established!");
                    return;
                }
                sendMessage(sender, "§bPolar Logs §8- §7Addon by §cShorty");
                if (timeRange != null)
                    sendMessage(sender, "§7Time range: §b" + timeRange.formatStart(polarLogs.getLogs().getTimestampFormat()) + " - " + timeRange.formatEnd(polarLogs.getLogs().getTimestampFormat()));
                sendMessage(sender, "§7Player info §b" + name2 + "§7@§b" + context + "§7:");
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
                    sendMessage(sender, mitigations.toLegacyText());
                    sendMessage(sender, detections.toLegacyText());
                    sendMessage(sender, cloudDetections.toLegacyText());
                    sendMessage(sender, punishments.toLegacyText());
                }
            } catch (SQLException e) {
                sendMessage(sender, "§cContext not found");
            }
        });
    }

    @Override
    public List<String> onTabComplete(User user, String[] args) {
        if (args.length == 1) {
            return Stream.of("reload", "info", "trange", "webhooks", "view", "export")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("webhooks")) {
                return Collections.singletonList("test");
            } else if (args[0].equalsIgnoreCase("info")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("view") || args[0].equalsIgnoreCase("export")) {
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
                    return Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase().substring(2)))
                            .map(name -> "p:" + name)
                            .collect(Collectors.toList());
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
        } else if (args.length == 3 && args[0].equalsIgnoreCase("export")) {
            return Collections.singletonList("includeDetails");
        }
        return Collections.emptyList();
    }

    private boolean hasPermission(User user, String permission) {
        if (user == null) {
            return true;
        }
        return user.bukkitPlayer().map(player -> player.hasPermission(permission)).orElse(false);
    }

    private boolean sendMessage(User user, String message) {
        CommandSender sender;
        if (user == null) {
            sender = Bukkit.getConsoleSender();
        } else {
            sender = user.bukkitPlayer().orElse(null);
        }
        if (sender == null) {
            return false;
        }
        sender.sendMessage(message);
        return true;
    }
}
