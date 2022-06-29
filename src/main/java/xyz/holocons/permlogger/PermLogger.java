package xyz.holocons.permlogger;

import com.google.inject.Inject;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private LuckPerms luckPermsAPI;
    private ConfigurationNode config;
    private String endpoint;
    private PermListener listener;
    private boolean status = true;

    @Inject
    public PermLogger(ProxyServer server, Logger logger, @DataDirectory Path configDirectory) {
        this.logger = logger;
        this.server = server;
        this.loader = HoconConfigurationLoader.builder()
                .setPath(Paths.get(configDirectory.toString(), "config.hocon"))
                .setDefaultOptions(configurationOptions -> configurationOptions.withShouldCopyDefaults(true))
                .build();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        this.luckPermsAPI = LuckPermsProvider.get();
        this.config = loadConfig();
        this.endpoint = config.getNode("webhook").getString("INSERT WEBHOOK HERE");
        this.listener = new PermListener(this, luckPermsAPI);

        var commandMeta = server.getCommandManager().metaBuilder("permlogger").build();
        server.getCommandManager().register(commandMeta, new CommandHandler(this));
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        saveConfig(this.config);
    }

    public ConfigurationNode loadConfig() {
        try {
            return loader.load();
        } catch (IOException e) {
            logger.error("Failed to load config!", e);
            throw new RuntimeException(e);
        }
    }

    public void saveConfig(ConfigurationNode root) {
        try {
            loader.save(root);
        } catch (IOException e) {
            logger.error("Failed to save config!", e);
            throw new RuntimeException(e);
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public boolean isEnabled() {
        return status;
    }
}
