package xyz.holocons.permlogger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

@Plugin(
        id = "permlogger",
        name = "PermLogger",
        version = BuildConstants.VERSION,
        description = "Logs permission changes on LuckPerms",
        url = "mc.holocons.xyz",
        authors = {"TraceL"},
        dependencies = {
                @Dependency(id = "luckperms")
        }
)
public class PermLogger {

    private final Logger logger;
    private final ProxyServer server;
    private final HoconConfigurationLoader loader;

    private ConfigurationNode config;
    private URL webhookURL;
    private boolean enabled;

    @Inject
    public PermLogger(ProxyServer server, Logger logger, @DataDirectory Path configDirectory) {
        this.logger = logger;
        this.server = server;
        this.loader = HoconConfigurationLoader.builder()
                .path(configDirectory.resolve("config.hocon"))
                .defaultOptions(options -> options.shouldCopyDefaults(true))
                .build();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        reloadConfig();

        var listener = new PermListener(this);
        listener.subscribeToEvents();

        var commandMeta = server.getCommandManager().metaBuilder("permlogger").build();
        server.getCommandManager().register(commandMeta, new CommandHandler(this));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        saveConfig(this.config);
    }

    public void reloadConfig() {
        this.config = loadConfig();
        var webhookString = config.node("webhook").getString("INSERT WEBHOOK HERE");
        try {
            this.webhookURL = URI.create(webhookString).toURL();
            this.enabled = true;
        } catch (Exception e) {
            logger.error("Invalid webhook URL in config: " + webhookString, e);
            this.enabled = false;
        }
    }

    public ConfigurationNode loadConfig() {
        try {
            return loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config!", e);
        }
    }

    public void saveConfig(ConfigurationNode root) {
        try {
            loader.save(root);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save config!", e);
        }
    }

    public URL getWebhookURL() {
        return webhookURL;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
