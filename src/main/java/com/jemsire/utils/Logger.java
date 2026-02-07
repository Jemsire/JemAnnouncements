package com.jemsire.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import com.jemsire.plugin.AnnouncementPlugin;

import java.util.logging.Level;

/**
 * Structured logging utility for JemAnnouncements plugin
 * Provides consistent logging with proper error handling
 */
public class Logger {
    private static HytaleLogger logger;

    private Logger() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Gets the logger instance, initializing if necessary
     */
    private static HytaleLogger getLogger() {
        if (logger == null) {
            AnnouncementPlugin plugin = AnnouncementPlugin.get();
            if (plugin != null) {
                logger = plugin.getLogger();
            }
        }
        return logger;
    }

    /**
     * Logs a message at the specified level
     * @param message The message to log
     * @param level The log level
     */
    public static void log(String message, Level level) {
        if (shouldLog(level)) {
            return;
        }

        HytaleLogger loggerInstance = getLogger();
        if (loggerInstance != null) {
            loggerInstance.at(level).log(message);
        } else {
            // Fallback to system out if logger not available
            System.out.println("[" + level + "] " + message);
        }
    }

    /**
     * Determines if a message should be logged based on the configured log level
     */
    private static boolean shouldLog(Level level) {
        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            return false; // Log everything if plugin not yet available
        }

        String configuredLevel = "INFO";
        try {
            configuredLevel = plugin.getAnnouncementConfig().get().getLogLevel().toUpperCase();
        } catch (Exception e) {
            // Use default if config not available
        }

        if (configuredLevel.equals("NONE")) {
            return level.intValue() < Level.SEVERE.intValue(); // Always log errors
        }

        if (configuredLevel.equals("DEBUG")) {
            return false; // Log everything
        }

        // Default: INFO
        return level.intValue() < Level.INFO.intValue();
    }

    /**
     * Logs an info message
     */
    public static void info(String message) {
        log(message, Level.INFO);
    }

    /**
     * Logs a debug message
     */
    public static void debug(String message) {
        log(message, Level.FINE);
    }

    /**
     * Logs a warning message
     */
    public static void warning(String message) {
        log(message, Level.WARNING);
    }

    /**
     * Logs a severe/error message
     */
    public static void severe(String message) {
        log(message, Level.SEVERE);
    }

    /**
     * Logs a severe/error message with exception
     */
    public static void severe(String message, Throwable throwable) {
        if (shouldLog(Level.SEVERE)) {
            return;
        }

        HytaleLogger loggerInstance = getLogger();
        if (loggerInstance != null) {
            loggerInstance.at(Level.SEVERE).log(message + ": " + throwable.getMessage());
            // We still use printStackTrace for detailed debugging in console if needed
            throwable.printStackTrace();
        } else {
            System.err.println("[SEVERE] " + message + ": " + throwable.getMessage());
            throwable.printStackTrace();
        }
    }
}
