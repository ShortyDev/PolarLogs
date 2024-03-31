package at.shorty.polar.addon.commands;

import at.shorty.polar.addon.PolarWebhooks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class PolarWebhooksCommand extends Command {

    private final PolarWebhooks polarWebhooks;
    private final String command;

    public PolarWebhooksCommand(PolarWebhooks polarWebhooks, String command) {
        super(command, "Polar Webhooks Command", "/polarwebhooks", new ArrayList<>());
        this.polarWebhooks = polarWebhooks;
        this.command = command;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!commandSender.hasPermission("polarwebhooks")) {
            commandSender.sendMessage("§cYou do not have permission to execute this command!");
            return true;
        }
        if (s.equalsIgnoreCase(command)) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                commandSender.sendMessage("§8Hint: Not respecting changes to the \"command\" config value! (Restart required)");
                commandSender.sendMessage("§7Reloading config...");
                polarWebhooks.reloadPluginConfig();
                commandSender.sendMessage("§aConfig reloaded!");
                return true;
            }
            commandSender.sendMessage("§bPolar §7Webhooks Addon by §cShorty");
            commandSender.sendMessage("§7Command permission: polarwebhooks");
            commandSender.sendMessage("§b/" + command + " reload §7- Reload the config");
            return true;
        }
        return false;
    }
}
