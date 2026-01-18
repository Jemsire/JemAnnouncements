package com.jemsire.utils;

import com.jemsire.config.AnnouncementConfig;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.plugin.AnnouncementPlugin;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Schedules and manages announcement messages based on configuration.
 * Handles both sequential and random ordering.
 */
public class AnnouncementScheduler {
    private static ScheduledExecutorService scheduler;
    private static ScheduledFuture<?> scheduledTask;
    private static final AtomicInteger sequentialIndex = new AtomicInteger(0);
    private static boolean isRunning = false;
    
    /**
     * Starts the announcement scheduler
     */
    public static void start() {
        if (isRunning) {
            Logger.warning("Announcement scheduler is already running");
            return;
        }
        
        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            Logger.warning("Plugin instance not available, cannot start scheduler");
            return;
        }
        
        int messageCount = MessageLoader.getMessageCount();
        if (messageCount == 0) {
            Logger.warning("No messages loaded, scheduler will start but won't send anything");
            // Continue anyway - scheduler should always start
        }
        
        // Create scheduler with a single thread
        scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread thread = new Thread(r);
            thread.setName("AnnouncementScheduler");
            thread.setDaemon(true);
            return thread;
        });
        
        AnnouncementConfig config = plugin.getAnnouncementConfig().get();
        int intervalSeconds = config.getIntervalSeconds();
        
        // Schedule the first announcement immediately, then repeat at intervals
        scheduledTask = scheduler.scheduleAtFixedRate(
            AnnouncementScheduler::sendNextAnnouncement,
            0, // Initial delay: send immediately
            intervalSeconds,
            TimeUnit.SECONDS
        );
        
        isRunning = true;
        Logger.info("Announcement scheduler started with interval of " + intervalSeconds + " seconds");
    }
    
    /**
     * Stops the announcement scheduler
     */
    public static void stop() {
        if (!isRunning) {
            return;
        }
        
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
            scheduledTask = null;
        }
        
        if (scheduler != null) {
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
            scheduler = null;
        }
        
        isRunning = false;
        Logger.info("Announcement scheduler stopped");
    }
    
    /**
     * Restarts the scheduler (useful after config reload)
     */
    public static void restart() {
        stop();
        // Small delay to ensure cleanup
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        start();
    }
    
    /**
     * Sends the next announcement based on the configured order type
     */
    private static void sendNextAnnouncement() {
        try {
            AnnouncementPlugin plugin = AnnouncementPlugin.get();
            if (plugin == null) {
                return;
            }
            
            AnnouncementConfig config = plugin.getAnnouncementConfig().get();
            
            int messageCount = MessageLoader.getMessageCount();
            if (messageCount == 0) {
                Logger.warning("No messages available to send");
                return;
            }
            
            AnnouncementMessage message;
            
            if (config.isRandom()) {
                // Get random message
                message = MessageLoader.getRandomMessage();
            } else {
                // Get next sequential message
                int currentIndex = sequentialIndex.getAndIncrement();
                message = MessageLoader.getNextSequentialMessage(currentIndex);
            }
            
            if (message != null) {
                String orderType = config.isRandom() ? "random" : "sequential";
                Logger.info("Sending announcement message (order: " + orderType + ")");
                MessageSender.sendAnnouncement(message);
                Logger.info("Announcement sent successfully");
            } else {
                Logger.warning("Failed to get announcement message");
            }
        } catch (Exception e) {
            Logger.severe("Error sending announcement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Checks if the scheduler is currently running
     */
    public static boolean isRunning() {
        return isRunning;
    }
}
