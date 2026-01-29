package com.jemsire.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.plugin.AnnouncementPlugin;

import java.util.List;

/**
 * Utility class for sending different types of announcement messages to players.
 * Supports chat, action bar, title, and sound messages.
 */
public class MessageSender {

    /**
     * Sends an announcement message to all online players
     */
    public static void sendAnnouncement(AnnouncementMessage message) {
        if (message == null) {
            Logger.warning("Message is null, cannot send announcement");
            return;
        }

        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            Logger.warning("Plugin instance not available, cannot send announcement");
            return;
        }

        Universe universe = Universe.get();
        if (universe == null) {
            Logger.warning("Universe not available, cannot send announcement");
            return;
        }

        // Get all online players
        List<PlayerRef> players = universe.getPlayers();
        if (players.isEmpty()) {
            Logger.info("No players online, skipping announcement");
            return; // No players online
        }

        // Send chat messages if present
        if (message.hasChatMessages()) {
            sendChatMessages(players, message.getChatMessages(), message.isCenterChat());
        }

        // Send notification if present
        if (message.hasNotification()) {
            sendNotification(players, message.getNotification());
        }

        // Send title if present
        if (message.hasTitle()) {
            sendTitle(players, message.getTitle());
        }

        // Play sound if present
        if (message.hasSound()) {
            playSound(players, message.getSound());
        }
    }

    /**
     * Sends chat messages to all players (in order).
     * Uses TinyMsg API: parses tags like &lt;color:X&gt;, &lt;gradient:X:Y&gt;, &lt;b&gt;, &lt;link:url&gt;.
     * Legacy &amp; color codes are converted to TinyMsg tags before parsing.
     * Messages are centered if the message's Center setting is enabled.
     * One &lt;offset:N&gt; per line adjusts centering: positive = more leading spaces (shift right), negative = fewer (shift left). Tag is stripped and not shown.
     */
    private static void sendChatMessages(List<PlayerRef> players, String[] chatMessages, boolean shouldCenter) {
        if (chatMessages == null || chatMessages.length == 0) {
            Logger.warning("Chat messages array was empty");
            return;
        }

        // Send each message in order
        for (String chatMessage : chatMessages) {
            if (chatMessage == null || chatMessage.isEmpty()) {
                continue; // Skip empty messages
            }

            // One <offset:N> per line: positive = more leading spaces, negative = fewer (stripped before display)
            int offset = ColorUtils.getOffset(chatMessage);
            String withoutOffsetTags = ColorUtils.stripOffsetTags(chatMessage);
            String messageToSend = shouldCenter ? centerText(withoutOffsetTags, offset) : withoutOffsetTags;

            // Parse with TinyMsg API (tags: <color:X>, <gradient:X:Y>, <b>, <link:url>, etc.); legacy & codes converted first
            String processedMessage = ColorUtils.convertLegacyColorCodes(messageToSend);
            Message message = TinyMsg.parse(processedMessage);

            // Send to all players
            for (PlayerRef player : players) {
                try {
                    player.sendMessage(message);
                } catch (Exception e) {
                    Logger.warning("Failed to send chat message to " + player.getUsername() + ": " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Centers text by calculating display width and adding leading spaces.
     * Uses visible width (tags/hex not counted). Optional offset adjusts: positive = more spaces (shift right), negative = fewer (shift left).
     * @param text The text to center (may contain TinyMsg tags and legacy &amp; codes; &lt;offset:N&gt; should already be stripped)
     * @param offset Adjustment to leading spaces (e.g. from &lt;offset:5&gt; or &lt;offset:-2&gt; tags)
     * @return The centered text with spaces prepended
     */
    private static String centerText(String text, int offset) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        int displayWidth = ColorUtils.getVisibleWidthForCentering(text);
        int centerWidth = 80;
        int spacesNeeded = (centerWidth - displayWidth) / 2 + offset;
        return " ".repeat(Math.max(0, spacesNeeded)) + text;
    }


    /**
     * Sends a notification to all players (similar to item pickup notifications)
     */
    private static void sendNotification(List<PlayerRef> players, AnnouncementMessage.NotificationConfig notificationConfig) {
        if (notificationConfig == null) {
            return;
        }

        // Parse title and subtitle using TinyMsg API
        String processedTitle = ColorUtils.convertLegacyColorCodes(notificationConfig.getTitle());
        String processedSubtitle = ColorUtils.convertLegacyColorCodes(notificationConfig.getSubtitle());

        Message titleMessage = TinyMsg.parse(processedTitle);
        Message subtitleMessage = processedSubtitle != null && !processedSubtitle.isEmpty()
                ? TinyMsg.parse(processedSubtitle)
                : Message.empty();

        // Get icon if specified, otherwise use null (optional)
        ItemWithAllMetadata icon = null;
        if (notificationConfig.hasIcon()) {
            try {
                icon = new ItemStack(notificationConfig.getIcon(), 1).toPacket();
            } catch (Exception e) {
                Logger.warning("Failed to create icon for notification: " + e.getMessage());
                // Continue without icon
            }
        }

        // Send notification to all players
        for (PlayerRef player : players) {
            try {
                var packetHandler = player.getPacketHandler();
                NotificationUtil.sendNotification(
                        packetHandler,
                        titleMessage,
                        subtitleMessage,
                        icon);
            } catch (Exception e) {
                Logger.warning("Failed to send notification to " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Sends a title/subtitle to all players.
     * Title and subtitle.
     */
    private static void sendTitle(List<PlayerRef> players, AnnouncementMessage.TitleConfig titleConfig) {
        if (titleConfig == null) {
            return;
        }

        String titleText = titleConfig.getTitle();
        String subtitleText = titleConfig.getSubtitle();

        // Parse title does not support color text.
        Message titleMessage;
        if (titleText != null && !titleText.isEmpty()) {
            String processedTitle = ColorUtils.convertLegacyColorCodes(titleText);
            String plainTitle = ColorUtils.stripColorCodes(processedTitle);
            titleMessage = Message.raw(plainTitle);
        } else {
            titleMessage = Message.raw("");
        }

        Message subtitleMessage;
        if (subtitleText != null && !subtitleText.isEmpty()) {
            String processedSubtitle = ColorUtils.convertLegacyColorCodes(subtitleText);
            String plainSubtitle = ColorUtils.stripColorCodes(processedSubtitle);
            subtitleMessage = Message.raw(plainSubtitle);
        } else {
            subtitleMessage = Message.raw("");
        }

        // Send title to all players
        // Parameters: player, title, subtitle, isMajor, icon, stay, fadeIn, fadeOut
        for (PlayerRef player : players) {
            try {
                EventTitleUtil.showEventTitleToPlayer(
                        player,
                        titleMessage,
                        subtitleMessage,
                        titleConfig.isMajor(), // isMajor - adds gold border if true
                        null, // icon
                        titleConfig.getStay(),
                        titleConfig.getFadeIn(),
                        titleConfig.getFadeOut()
                );
            } catch (Exception e) {
                Logger.warning("Failed to send title to " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Plays a sound to all players
     */
    private static void playSound(List<PlayerRef> players, AnnouncementMessage.SoundConfig soundConfig) {
        if (soundConfig == null || soundConfig.getSoundName() == null || soundConfig.getSoundName().isEmpty()) {
            return;
        }

        String soundName = soundConfig.getSoundName();
        float volume = soundConfig.getVolume();
        float pitch = soundConfig.getPitch();

        // Play sound to all players
        // Note: Hytale API may have a specific method for playing sounds
        for (PlayerRef player : players) {
            try {
                int index = SoundEvent.getAssetMap().getIndex(soundName);
                World world = Universe.get().getWorld(player.getWorldUuid());
                EntityStore store = world.getEntityStore();
                Ref<EntityStore> playerRef = player.getReference();
                world.execute(() -> {
                    TransformComponent transform = store.getStore().getComponent(playerRef, EntityModule.get().getTransformComponentType());
                    SoundUtil.playSoundEvent3dToPlayer(playerRef, index, SoundCategory.UI, transform.getPosition(), store.getStore());
                });
            } catch (Exception e) {
                Logger.warning("Failed to play sound to " + player.getUsername() + ": " + e.getMessage());
            }
        }
    }

}
