package xyz.holocons.permlogger;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class CommandHandler implements SimpleCommand {

    private final PermLogger plugin;

    public CommandHandler(PermLogger plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation) {
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

        invocation.source().sendMessage(component.build());
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("permlogger.admin");
    }
}
