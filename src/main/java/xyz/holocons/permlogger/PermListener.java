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

    public void onLogReceive(LogReceiveEvent event) {
        if (!plugin.isEnabled()) {
            plugin.getLogger().warn("Permission changed detected, but the plugin is disabled!");
            return;
        }

        var message = event.getEntry().getSource().getName() +
                " executed `" + event.getEntry().getDescription() + "` for " +
                event.getEntry().getTarget().getType() + "#" + event.getEntry().getTarget().getName() +
                "\\n";

        plugin.getLogger().info("Posting to discord webhook with content: " + message);
        debouncer.postMessage(message);
    }

    public void onLogPublish(LogNetworkPublishEvent event) {
        if (!plugin.isEnabled()) {
            plugin.getLogger().warn("Permission changed detected, but the plugin is disabled!");
            return;
        }

        var message = event.getEntry().getSource().getName() +
                " executed `" + event.getEntry().getDescription() + "` for " +
                event.getEntry().getTarget().getType() + "#" + event.getEntry().getTarget().getName() +
                "\\n";

        plugin.getLogger().info("Posting to discord webhook with content: " + message);
        debouncer.postMessage(message);
    }
}
