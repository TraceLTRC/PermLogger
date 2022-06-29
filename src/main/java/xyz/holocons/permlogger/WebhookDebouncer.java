package xyz.holocons.permlogger;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Debounces webhook calls so the plugin doesn't spam the discord API
 */
public class WebhookDebouncer {

    private final long delay;
    private final PermLogger plugin;
    private final DiscordWebhook endpoint;
    private final StringBuilder messageQueue = new StringBuilder();

    private ScheduledTask task;

    public WebhookDebouncer(PermLogger plugin, long delay) {
        this.delay = delay;
        this.plugin = plugin;
        this.endpoint = new DiscordWebhook(plugin.getEndpoint());
        this.endpoint.setUsername("Permission Logger");
        this.endpoint.setAvatarUrl("https://wiki.holocons.xyz/hlc_logo.png");
    }

    /**
     * Post a message to discord after delay. Appends new messages if this function is called again before the message
     * gets posted
     * @param message The message to post
     */
    public void postMessage(String message) {
        if (task != null) { // There is a message queued
            if (task.status() == TaskStatus.FINISHED) { // Message has been posted to discord, create a new task
                messageQueue.setLength(0);
                messageQueue.append(message);
            } else { // Message is on queue, cancel it and recreate the task.
                task.cancel();
                messageQueue.append(message);
            }
        } else { // No message has been queued, create a new task
            messageQueue.append(message);
        }

        task = plugin.getServer().getScheduler().buildTask(plugin, () -> {
            endpoint.setContent(messageQueue.toString());
            try {
                endpoint.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).delay(delay, TimeUnit.MILLISECONDS).schedule();
    }
}
