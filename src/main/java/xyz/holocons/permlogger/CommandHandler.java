package xyz.holocons.permlogger;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Locale;

public class CommandHandler implements SimpleCommand {

    private final PermLogger plugin;

    public CommandHandler(PermLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
        var source = invocation.source();
        var args = invocation.arguments();

        if (!hasPermission(invocation)) {
            source.sendMessage(Component.text("You do not have enough permission!"));
            return;
        }

        if (args.length == 0) {
            source.sendMessage(createOverview());
        } else {
            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> {
                    plugin.reloadConfig();
                    plugin.getLogger().info("Plugin reloaded!");
                }
                case "on" -> {
                    plugin.setStatus(true);
                    plugin.getLogger().info("Enabled plugin!");
                }
                case "off" -> {
                    plugin.setStatus(false);
                    plugin.getLogger().info("Disabled plugin!");
                }

                default -> plugin.getLogger().info("/permlogger <on/off/reload>");
            }
        }

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("permlogger.admin");
    }

    public Component createOverview() {
        final var component = Component.text();
        component.append(Component.text("PermLogger is "));

        if (plugin.isEnabled()) {
            component.append(
                    Component.text("ENABLED").color(NamedTextColor.GREEN)
            );
        } else {
            component.append(
                    Component.text("DISABLED").color(NamedTextColor.RED)
            );
        }

        component.append(
                Component.newline(),
                Component.text("Current endpoint: "),
                Component.text(plugin.getEndpoint()).color(NamedTextColor.DARK_AQUA)
        );
        return component.build();
    }
}
