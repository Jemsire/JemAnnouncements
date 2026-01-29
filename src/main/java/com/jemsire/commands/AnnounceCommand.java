package com.jemsire.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
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

    private final RequiredArg<String> messageNameArg = this.withRequiredArg("message-name", "The name of the announcement message to send", ArgTypes.STRING);

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

        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            context.sendMessage(Message.raw("Plugin instance not available!").color(Color.RED));
            return;
        }

        // Get the message name from the required argument
        String messageName = context.get(messageNameArg);

        // Remove .json extension if provided
        String configKey = messageName;
        if (configKey.endsWith(".json")) {
            configKey = configKey.substring(0, configKey.length() - 5);
        }

        Map<String, Config<AnnouncementMessage>> messageConfigs = plugin.getMessageConfigs();
        Config<AnnouncementMessage> messageConfig = messageConfigs.get(configKey);
        AnnouncementMessage message;

        if (messageConfig != null) {
            try {
                messageConfig.load();
                message = messageConfig.get();
            } catch (Exception e) {
                context.sendMessage(Message.raw("Error loading message: " + e.getMessage()).color(Color.RED));
                return;
            }
        } else {
            // Check dynamically loaded messages (new files added after startup)
            message = plugin.getDynamicMessageConfigs().get(configKey);
        }

        if (message == null) {
            java.util.Set<String> allNames = new java.util.TreeSet<>(messageConfigs.keySet());
            allNames.addAll(plugin.getDynamicMessageConfigs().keySet());
            context.sendMessage(Message.raw("Message '" + messageName + "' not found!").color(Color.RED));
            context.sendMessage(Message.raw("Available messages: " + String.join(", ", allNames)).color(Color.GRAY));
            return;
        }

        if (!message.isEnabled()) {
            context.sendMessage(Message.raw("Message '" + messageName + "' is disabled!").color(Color.YELLOW));
            return;
        }

        try {
            MessageSender.sendAnnouncement(message);
            context.sendMessage(Message.raw("Announcement '" + messageName + "' sent successfully!").color(Color.GREEN));
        } catch (Exception e) {
            context.sendMessage(Message.raw("Error sending announcement: " + e.getMessage()).color(Color.RED));
        }
    }
}
