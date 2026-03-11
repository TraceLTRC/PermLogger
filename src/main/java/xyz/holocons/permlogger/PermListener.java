package xyz.holocons.permlogger;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.log.LogNetworkPublishEvent;
import net.luckperms.api.event.log.LogReceiveEvent;

public class PermListener {
    private final PermLogger plugin;
    private final WebhookDebouncer debouncer;

    public PermListener(PermLogger plugin) {
        this.plugin = plugin;
        this.debouncer = new WebhookDebouncer(plugin, 5000);
    }

    public void subscribeToEvents() {
        var luckPerms = LuckPermsProvider.get();
        var eventBus = luckPerms.getEventBus();
        eventBus.subscribe(plugin, LogReceiveEvent.class, this::onLogReceive);
        eventBus.subscribe(plugin, LogNetworkPublishEvent.class, this::onLogPublish);
    }

    private void onLogReceive(LogReceiveEvent event) {
        handleLogEntry(LogEntry.fromEvent(event));
    }

    private void onLogPublish(LogNetworkPublishEvent event) {
        handleLogEntry(LogEntry.fromEvent(event));
    }

    private void handleLogEntry(LogEntry entry) {
        if (plugin.isEnabled()) {
            plugin.getLogger().info("Posting to webhook: " + entry.toString());
            debouncer.postMessage(entry.toDiscordMessage());
        } else {
            plugin.getLogger().warn("A permission changed, but the plugin is disabled!");
        }
    }

    private record LogEntry(String source, String action, String target) {

        public static LogEntry fromEvent(LogReceiveEvent event) {
            var source = event.getEntry().getSource().getName();
            var action = "`" + event.getEntry().getDescription() + "`";
            var target = event.getEntry().getTarget().getType() + "#" + event.getEntry().getTarget().getName();
            return new LogEntry(source, action, target);
        }

        private static LogEntry fromEvent(LogNetworkPublishEvent event) {
            var source = event.getEntry().getSource().getName();
            var action = "`" + event.getEntry().getDescription() + "`";
            var target = event.getEntry().getTarget().getType() + "#" + event.getEntry().getTarget().getName();
            return new LogEntry(source, action, target);
        }

        private static String escapeMarkdown(String input) {
            return input.replace("_", "\\\\_");
        }

        public String toDiscordMessage() {
            return escapeMarkdown(toString()) + "\\n";
        }

        @Override
        public String toString() {
            return source + " executed " + action + " for " + target;
        }
    }
}
