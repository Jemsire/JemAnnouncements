package com.jemsire.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.plugin.AnnouncementPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * Loads announcement messages from JSON files in the messages folder.
 * Dynamically discovers and loads all .json files.
 */
public class MessageLoader {
    private static final String MESSAGES_FOLDER = "messages";
    private static List<AnnouncementMessage> loadedMessages = new ArrayList<>();
    private static Map<String, Config<AnnouncementMessage>> messageConfigs = new HashMap<>();
    
    /**
     * Loads all message files from the messages folder.
     * Configs must already be registered in the plugin constructor.
     */
    public static void loadMessages() {
        loadedMessages.clear();
        
        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            Logger.warning("Plugin instance not available, cannot load messages");
            return;
        }
        
        Map<String, Config<AnnouncementMessage>> configs = plugin.getMessageConfigs();
        
        if (configs.isEmpty()) {
            Logger.info("No message configs registered");
            return;
        }
        
        Logger.info("Loading " + configs.size() + " message config(s)");
        
        // Load each message config
        int loadedCount = 0;
        for (Map.Entry<String, Config<AnnouncementMessage>> entry : configs.entrySet()) {
            try {
                String configName = entry.getKey();
                Config<AnnouncementMessage> config = entry.getValue();
                
                // Load the config
                config.load();
                
                // Get the message data (must call .get() after .load() to get updated data)
                AnnouncementMessage message = config.get();
                if (message != null) {
                    // Only load enabled messages
                    if (message.isEnabled()) {
                        loadedMessages.add(message);
                        messageConfigs.put(configName, config);
                        loadedCount++;
                        Logger.info("Loaded message: " + configName);
                    } else {
                        Logger.info("Skipped disabled message: " + configName);
                    }
                } else {
                    Logger.warning("Failed to load message from: " + configName);
                }
            } catch (Exception e) {
                Logger.severe("Error loading message config " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        // Sort by priority (higher priority first)
        loadedMessages.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        Logger.info("Successfully loaded " + loadedCount + " message(s)");
    }
    
    /**
     * Reloads all message files.
     * Calls .load() then .get() on each config to get updated data.
     */
    public static void reloadMessages() {
        Logger.info("Reloading messages...");
        
        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            Logger.warning("Plugin instance not available, cannot reload messages");
            return;
        }
        
        // Clear current messages
        loadedMessages.clear();
        
        Map<String, Config<AnnouncementMessage>> configs = plugin.getMessageConfigs();
        
        // Reload each config and get updated data
        int loadedCount = 0;
        for (Map.Entry<String, Config<AnnouncementMessage>> entry : configs.entrySet()) {
            try {
                String configName = entry.getKey();
                Config<AnnouncementMessage> config = entry.getValue();
                
                // Reload the config
                config.load();
                
                // Get the updated message data (must call .get() after .load())
                AnnouncementMessage message = config.get();
                if (message != null) {
                    // Only load enabled messages
                    if (message.isEnabled()) {
                        loadedMessages.add(message);
                        messageConfigs.put(configName, config);
                        loadedCount++;
                    }
                }
            } catch (Exception e) {
                Logger.severe("Error reloading message config " + entry.getKey() + ": " + e.getMessage());
            }
        }
        
        // Sort by priority (higher priority first)
        loadedMessages.sort((a, b) -> Integer.compare(b.getPriority(), a.getPriority()));
        
        Logger.info("Successfully reloaded " + loadedCount + " message(s)");
    }
    
    /**
     * Gets all loaded messages
     */
    public static List<AnnouncementMessage> getLoadedMessages() {
        return new ArrayList<>(loadedMessages);
    }
    
    /**
     * Gets a random message from the loaded messages
     */
    public static AnnouncementMessage getRandomMessage() {
        if (loadedMessages.isEmpty()) {
            return null;
        }
        Random random = new Random();
        return loadedMessages.get(random.nextInt(loadedMessages.size()));
    }
    
    /**
     * Gets the next message in sequential order (with index tracking)
     */
    public static AnnouncementMessage getNextSequentialMessage(int currentIndex) {
        if (loadedMessages.isEmpty()) {
            return null;
        }
        return loadedMessages.get(currentIndex % loadedMessages.size());
    }
    
    /**
     * Gets the number of loaded messages
     */
    public static int getMessageCount() {
        return loadedMessages.size();
    }
    
}
