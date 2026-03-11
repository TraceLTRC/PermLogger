package xyz.holocons.permlogger;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.log.LogNetworkPublishEvent;
import net.luckperms.api.event.log.LogReceiveEvent;

public class PermListener {
    private final PermLogger plugin;
    private final WebhookDebouncer debouncer;

    public PermListener(PermLogger plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.debouncer = new WebhookDebouncer(plugin, 5000);

        var eventBus = luckPerms.getEventBus();
        eventBus.subscribe(plugin, LogReceiveEvent.class, this::onLogReceive);
        eventBus.subscribe(plugin, LogNetworkPublishEvent.class, this::onLogPublish);
    }

    private String escapeMarkdown(String input) {
        return input.replace("_", "\\\\_");
    }

    private void onLogReceive(LogReceiveEvent event) {
        if (!plugin.isEnabled()) {
            plugin.getLogger().warn("A permission changed, but the plugin is disabled!");
            return;
        }

        var source = escapeMarkdown(event.getEntry().getSource().getName());
        var description = "`" + event.getEntry().getDescription() + "`";
        var target = event.getEntry().getTarget().getType() + "#"
                + escapeMarkdown(event.getEntry().getTarget().getName());
        var message = source + " executed " + description + " for " + target + "\\n";

        plugin.getLogger().info("Posting to webhook: " + message);
        debouncer.postMessage(message);
    }

    private void onLogPublish(LogNetworkPublishEvent event) {
        if (!plugin.isEnabled()) {
            plugin.getLogger().warn("A permission changed, but the plugin is disabled!");
            return;
        }

        var source = escapeMarkdown(event.getEntry().getSource().getName());
        var description = "`" + event.getEntry().getDescription() + "`";
        var target = event.getEntry().getTarget().getType() + "#"
                + escapeMarkdown(event.getEntry().getTarget().getName());
        var message = source + " executed " + description + " for " + target + "\\n";

        plugin.getLogger().info("Posting to webhook: " + message);
        debouncer.postMessage(message);
    }
}
