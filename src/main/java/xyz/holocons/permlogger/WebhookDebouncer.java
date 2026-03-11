package xyz.holocons.permlogger;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;

import java.util.concurrent.TimeUnit;

/**
 * Debounces webhook calls so the plugin doesn't spam the discord API
 */
public class WebhookDebouncer {

    private final long delay;
    private final PermLogger plugin;
    private final DiscordWebhook webhook;
    private final StringBuilder queue;

    private int lastMessageHash;
    private ScheduledTask task;

    public WebhookDebouncer(PermLogger plugin, long delay) {
        this.delay = delay;
        this.plugin = plugin;
        this.webhook = new DiscordWebhook();
        this.queue = new StringBuilder();
        this.webhook.setUsername("Permission Logger");
        this.webhook.setAvatarUrl("https://wiki.holocons.xyz/hlc_logo.png");
        this.task = scheduleTask(() -> {
        });
    }

    private ScheduledTask scheduleTask(Runnable runnable) {
        return plugin.getServer().getScheduler()
                .buildTask(plugin, runnable)
                .delay(delay, TimeUnit.MILLISECONDS)
                .schedule();
    }

    public void postMessage(String message) {
        if (message.hashCode() == lastMessageHash) {
            return;
        }
        lastMessageHash = message.hashCode();

        queue.append(message);

        if (task.status() == TaskStatus.SCHEDULED) {
            task.cancel();
        }
        task = scheduleTask(() -> {
            webhook.setContent(queue.toString());
            queue.setLength(0);
            webhook.execute(plugin.getWebhookURL());
        });
    }
}
