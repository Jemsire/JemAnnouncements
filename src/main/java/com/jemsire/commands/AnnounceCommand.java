package com.jemsire.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.util.Config;
import com.jemsire.config.AnnouncementMessage;
import com.jemsire.plugin.AnnouncementPlugin;
import com.jemsire.utils.MessageSender;

import javax.annotation.Nonnull;
import java.awt.Color;
import java.util.Map;

/**
 * Command to manually trigger an announcement by message name.
 * Usage: /announce <message-name>
 * Permission: jemsire.announcements.announce
 */
public class AnnounceCommand extends CommandBase {

    public AnnounceCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        // Check permission for players
        if (context.isPlayer()) {
            if (!context.sender().hasPermission("jemsire.announcements.announce")) {
                context.sendMessage(Message.raw("You do not have permission to perform this command!").color(Color.RED));
                return;
            }
        }

        // Check if message name argument is provided
        if (context.getInputString().isEmpty()) {
            context.sendMessage(Message.raw("Usage: /announce <message-name>").color(Color.YELLOW));
            context.sendMessage(Message.raw("Example: /announce example").color(Color.GRAY));
            return;
        }

        String messageName = context.getInputString().split(" ")[0];

        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            context.sendMessage(Message.raw("Plugin instance not available!").color(Color.RED));
            return;
        }

        // Get the message config by name (configs are stored without .json extension)
        Map<String, Config<AnnouncementMessage>> messageConfigs = plugin.getMessageConfigs();
        
        // Remove .json extension if provided
        String configKey = messageName;
        if (configKey.endsWith(".json")) {
            configKey = configKey.substring(0, configKey.length() - 5);
        }

        Config<AnnouncementMessage> messageConfig = messageConfigs.get(configKey);
        
        if (messageConfig == null) {
            context.sendMessage(Message.raw("Message '" + messageName + "' not found!").color(Color.RED));
            context.sendMessage(Message.raw("Available messages: " + String.join(", ", messageConfigs.keySet())).color(Color.GRAY));
            return;
        }

        // Load the message config
        try {
            messageConfig.load();
            AnnouncementMessage message = messageConfig.get();
            
            if (message == null) {
                context.sendMessage(Message.raw("Failed to load message '" + messageName + "'!").color(Color.RED));
                return;
            }

            // Check if message is enabled
            if (!message.isEnabled()) {
                context.sendMessage(Message.raw("Message '" + messageName + "' is disabled!").color(Color.YELLOW));
                return;
            }

            // Send the announcement
            MessageSender.sendAnnouncement(message);
            context.sendMessage(Message.raw("Announcement '" + messageName + "' sent successfully!").color(Color.GREEN));
            
        } catch (Exception e) {
            context.sendMessage(Message.raw("Error sending announcement: " + e.getMessage()).color(Color.RED));
        }
    }
}
