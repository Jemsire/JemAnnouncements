package com.jemsire.plugin;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.commands.AnnounceCommand;
import com.jemsire.commands.ReloadCommand;
import com.jemsire.config.AnnouncementConfig;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.utils.AnnouncementScheduler;
import com.jemsire.utils.Logger;
import com.jemsire.utils.MessageLoader;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
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

    public static AnnouncementPlugin get() {
        return instance;
    }

    private Config<AnnouncementConfig> announcementConfig;
    private Map<String, Config<AnnouncementMessage>> messageConfigs = new HashMap<>();

    public AnnouncementPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        Logger.info("Starting JemAnnouncements Plugin...");

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
            File pluginDataDir = this.getDataDirectory().toFile();
            File messagesDir = new File(pluginDataDir, "messages");

            // Create messages directory if it doesn't exist
            if (!messagesDir.exists()) {
                messagesDir.mkdirs();
            }

            // Create example templates if they don't exist
            createExampleTemplates(messagesDir);

            // Find all .json files in the messages directory
            List<File> messageFiles = new java.util.ArrayList<>();
            try (Stream<Path> paths = Files.walk(messagesDir.toPath())) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                        .map(Path::toFile)
                        .forEach(messageFiles::add);
            }

            // Register config for each message file
            for (File messageFile : messageFiles) {
                String fileName = messageFile.getName();
                String configName = fileName.substring(0, fileName.lastIndexOf('.'));

                // Register config using withConfig (must be in constructor)
                Config<AnnouncementMessage> config = this.withConfig(
                        "messages/" + configName,
                        AnnouncementMessage.CODEC
                );
                messageConfigs.put(configName, config);
            }

            Logger.info("Registered " + messageConfigs.size() + " message config(s)");
        } catch (Exception e) {
            Logger.warning("Failed to discover message configs: " + e.getMessage());
        }
    }

    /**
     * Creates example template message files if they don't exist
     * Copies files from resources/messages/ to the plugin's messages directory
     */
    private void createExampleTemplates(File messagesDir) {
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
            copyExampleFileFromResources(messagesDir, fileName);
        }
    }

    /**
     * Copies an example file from resources to the messages directory if it doesn't exist
     */
    private void copyExampleFileFromResources(File messagesDir, String fileName) {
        File targetFile = new File(messagesDir, fileName);

        // Skip if file already exists
        if (targetFile.exists()) {
            return;
        }

        try {
            // Read from resources
            String resourcePath = "messages/" + fileName;
            InputStream resourceStream = getClass().getClassLoader().getResourceAsStream(resourcePath);

            if (resourceStream == null) {
                Logger.warning("Example file not found in resources: " + resourcePath);
                return;
            }

            // Copy to target location
            Files.copy(resourceStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            resourceStream.close();

            Logger.info("Created example message template: " + targetFile.getAbsolutePath());
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
    protected void shutdown() {
        Logger.info("Shutting down JemAnnouncements...");

        // Stop the scheduler
        AnnouncementScheduler.stop();

        // Save config
        announcementConfig.save();
        Logger.info("Config saved.");

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
     * Gets all registered message configs
     */
    public Map<String, Config<AnnouncementMessage>> getMessageConfigs() {
        return messageConfigs;
    }
}
