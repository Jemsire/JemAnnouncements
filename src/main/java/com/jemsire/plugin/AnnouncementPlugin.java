package com.jemsire.plugin;

import com.hypixel.hytale.common.semver.Semver;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.commands.AnnounceCommand;
import com.jemsire.commands.ReloadCommand;
import com.jemsire.config.AnnouncementConfig;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.expansion.JemAnnouncementsExpansion;
import com.jemsire.jemplaceholders.api.JemPlaceholdersAPI;
import com.jemsire.utils.AnnouncementScheduler;
import com.jemsire.utils.Logger;
import com.jemsire.utils.MessageLoader;
import com.jemsire.utils.UpdateChecker;

import javax.annotation.Nonnull;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Main plugin class for JemAnnouncements.
 * Manages configuration, message loading, and scheduling.
 */
public class AnnouncementPlugin extends JavaPlugin {
    private static AnnouncementPlugin instance;

    private final Semver version;

    public static AnnouncementPlugin get() {
        return instance;
    }

    private final Config<AnnouncementConfig> announcementConfig;
    private final Map<String, Config<AnnouncementMessage>> messageConfigs = new HashMap<>();
    /** Messages loaded from new .json files at reload (withConfig cannot be called after setup) */
    private final Map<String, AnnouncementMessage> dynamicMessageConfigs = new HashMap<>();

    public AnnouncementPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        Logger.info("Starting JemAnnouncements Plugin...");

        version = init.getPluginManifest().getVersion();

        // Register the main configuration
        this.announcementConfig = this.withConfig("AnnouncementConfig", AnnouncementConfig.CODEC);

        // Discover and register message configs in constructor (must be done before setup)
        discoverAndRegisterMessageConfigs();
    }

    /**
     * Discovers message files and registers their configs in the constructor.
     * This must happen before setup() as withConfig() can only be called in constructor.
     */
    private void discoverAndRegisterMessageConfigs() {
        try {
            Path messagesDir = this.getDataDirectory().resolve("messages");

            // Create messages directory if it doesn't exist
            if (!Files.exists(messagesDir)) {
                Files.createDirectories(messagesDir);
            }

            // Load main config to check for example creation
            announcementConfig.load();
            AnnouncementConfig configData = announcementConfig.get();

            // Create example templates if they don't exist and are enabled
            if (configData != null && configData.isCreateExampleMessages()) {
                createExampleTemplates(messagesDir);
            }

            // Find all .json files in the messages directory recursively
            List<Path> messageFiles = new java.util.ArrayList<>();
            try (Stream<Path> paths = Files.walk(messagesDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                        .forEach(messageFiles::add);
            }

            // Register config for each message file
            for (Path messageFile : messageFiles) {
                // Get relative path from messages directory
                Path relativePath = messagesDir.relativize(messageFile);
                String pathString = relativePath.toString().replace("\\", "/");
                String configPath = "messages/" + pathString.substring(0, pathString.lastIndexOf('.'));

                // Register config using withConfig (must be in constructor)
                Config<AnnouncementMessage> config = this.withConfig(
                        configPath,
                        AnnouncementMessage.CODEC
                );
                messageConfigs.put(pathString, config);
            }

            Logger.info("Registered " + messageConfigs.size() + " message config(s)");
        } catch (Exception e) {
            Logger.warning("Failed to discover message configs: " + e.getMessage());
        }
    }

    /**
     * Creates example template message files if they don't exist
     * Copies files from resources/messages/ to the plugin's messages/example directory
     */
    private void createExampleTemplates(Path messagesDir) {
        Path exampleDir = messagesDir.resolve("example");
        try {
            if (!Files.exists(exampleDir)) {
                Files.createDirectories(exampleDir);
            }
        } catch (Exception e) {
            Logger.warning("Failed to create example directory: " + e.getMessage());
            return;
        }

        // List of example files to copy from resources
        String[] exampleFiles = {
                "example.json",
                "example-chat.json",
                "example-notification.json",
                "example-title.json",
                "example-sound.json",
                "example-all.json",
                "example-no-center.json"
        };

        for (String fileName : exampleFiles) {
            copyExampleFileFromResources(exampleDir, fileName);
        }
    }

    /**
     * Copies an example file from resources to the messages directory if it doesn't exist
     */
    private void copyExampleFileFromResources(Path messagesDir, String fileName) {
        Path targetFile = messagesDir.resolve(fileName);

        // Skip if file already exists
        if (Files.exists(targetFile)) {
            return;
        }

        try {
            // Read from resources
            String resourcePath = "messages/" + fileName;
            try (InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                if (resourceStream == null) {
                    Logger.warning("Example file not found in resources: " + resourcePath);
                    return;
                }

                // Copy to target location
                Files.copy(resourceStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            Logger.info("Created example message template: " + targetFile.toAbsolutePath());
        } catch (Exception e) {
            Logger.warning("Failed to copy example template " + fileName + ": " + e.getMessage());
        }
    }

    @Override
    protected void setup() {
        instance = this;

        // Register commands
        registerCommands();

        // Save config (creates default if doesn't exist)
        announcementConfig.save();
        Logger.info("Config saved.");

        // Load messages from the messages folder
        MessageLoader.loadMessages();

        // Start the announcement scheduler
        AnnouncementScheduler.start();

        Logger.info("JemAnnouncements setup complete!");
    }

    @Override
    protected void start() {
        if (isJemPlaceholdersEnabled()) {
            JemPlaceholdersAPI.registerExpansion(new JemAnnouncementsExpansion());
        }

        if(announcementConfig.get().checkUpdates()){
            new UpdateChecker(version.toString()).checkForUpdatesAsync();
        }


        Logger.info("[JemAnnouncements] Started!");
        Logger.info("[JemAnnouncements] Use /jemp help for commands");
    }

    @Override
    protected void shutdown() {
        Logger.info("Shutting down JemAnnouncements...");

        // Stop the scheduler
        AnnouncementScheduler.stop();

        // Shutdown updater
        if(announcementConfig.get().checkUpdates()){
            UpdateChecker.shutdown();
        }

        // Save config
        announcementConfig.save();
        Logger.info("Config saved.");

        instance = null;
        Logger.info("JemAnnouncements shutdown complete.");
    }

    private void registerCommands() {
        this.getCommandRegistry().registerCommand(
                new ReloadCommand("announce-reload", "Reload the announcement configuration and messages")
        );
        this.getCommandRegistry().registerCommand(
                new AnnounceCommand("announce", "Manually trigger an announcement by message name")
        );
        Logger.info("Commands registered.");
    }

    /**
     * Gets the announcement configuration
     */
    public Config<AnnouncementConfig> getAnnouncementConfig() {
        return announcementConfig;
    }

    /**
     * Gets all registered message configs (from constructor / before setup)
     */
    public Map<String, Config<AnnouncementMessage>> getMessageConfigs() {
        return messageConfigs;
    }

    /**
     * Gets messages loaded from new .json files at reload (withConfig cannot be called after setup).
     */
    public Map<String, AnnouncementMessage> getDynamicMessageConfigs() {
        return dynamicMessageConfigs;
    }

    /**
     * Re-scans the messages directory and loads any new .json files manually (without withConfig).
     * withConfig() can only be run before setup, so new files added after startup are loaded via JSON parsing.
     */
    public void discoverAndRegisterNewMessageConfigs() {
        dynamicMessageConfigs.clear();
        try {
            Path messagesDir = this.getDataDirectory().resolve("messages");
            if (!Files.exists(messagesDir)) {
                return;
            }

            List<Path> messageFiles = new java.util.ArrayList<>();
            try (Stream<Path> paths = Files.walk(messagesDir)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                        .forEach(messageFiles::add);
            }

            for (Path messageFile : messageFiles) {
                // Get relative path from messages directory
                Path relativePath = messagesDir.relativize(messageFile);
                String configName = relativePath.toString().replace("\\", "/"); // Use full relative path as key

                if (messageConfigs.containsKey(configName)) {
                    continue;
                }
                try {
                    String json = Files.readString(messageFile, StandardCharsets.UTF_8);
                    AnnouncementMessage message = AnnouncementMessage.fromJson(json);
                    if (message != null) {
                        dynamicMessageConfigs.put(configName, message);
                        Logger.info("Loaded new message file on reload: " + configName);
                    } else {
                        Logger.warning("Failed to parse new message file: " + configName);
                    }
                } catch (Exception e) {
                    Logger.warning("Could not load new message file " + configName + ": " + e.getMessage());
                }
            }
            if (!dynamicMessageConfigs.isEmpty()) {
                Logger.info("Loaded " + dynamicMessageConfigs.size() + " new message file(s) on reload");
            }
        } catch (Exception e) {
            Logger.warning("Failed to discover new message files on reload: " + e.getMessage());
        }
    }

    public boolean isJemPlaceholdersEnabled() {
        try {
            Class.forName("com.jemsire.jemplaceholders.api.JemPlaceholdersAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
