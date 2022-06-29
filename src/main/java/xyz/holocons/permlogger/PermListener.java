package xyz.holocons.permlogger;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.log.LogReceiveEvent;
import net.luckperms.api.event.node.NodeMutateEvent;

import java.io.IOException;

public class PermListener {
    private final PermLogger plugin;
    private final WebhookDebouncer debouncer;

    public PermListener(PermLogger plugin, LuckPerms luckPerms) {
        this.plugin = plugin;
        this.debouncer = new WebhookDebouncer(plugin, 5000);

        var eventBus = luckPerms.getEventBus();
        eventBus.subscribe(plugin, LogReceiveEvent.class, this::onLogReceive);
        eventBus.subscribe(plugin, NodeMutateEvent.class, this::onNodeMutate);
    }

    public void onLogReceive(LogReceiveEvent event) {
        var message = event.getEntry().getSource().getName() +
                " executed `" + event.getEntry().getDescription() + "` for " +
                event.getEntry().getTarget().getType() + "#" + event.getEntry().getTarget().getName() +
                "\\n";

        plugin.getLogger().info("Posting to discord webhook with content: " + message);
        debouncer.postMessage(message);
    }

    public void onNodeMutate(NodeMutateEvent event) {
        plugin.getLogger().warn("Latest changes to permissions was not logged! Please log it through #staff-log.");
    }
}
