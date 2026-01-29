package com.jemsire.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.jemsire.plugin.AnnouncementPlugin;
import com.jemsire.utils.AnnouncementScheduler;
import com.jemsire.utils.MessageLoader;

import javax.annotation.Nonnull;
import java.awt.*;

public class ReloadCommand extends CommandBase {

    public ReloadCommand(String name, String description) {
        super(name, description);
    }

    @Override
    protected void executeSync(@Nonnull CommandContext context) {
        if(context.isPlayer()){
            if(!context.sender().hasPermission("jemsire.announcements.reload")){
                context.sendMessage(Message.raw("You do not have permission to perform this command!").color(Color.RED));
                return;
            }
        }

        AnnouncementPlugin plugin = AnnouncementPlugin.get();
        if (plugin == null) {
            context.sendMessage(Message.raw("Plugin instance not available!").color(Color.RED));
            return;
        }

        // Reload main config
        int oldInterval = plugin.getAnnouncementConfig().get().getIntervalSeconds();
        boolean oldRandomization = plugin.getAnnouncementConfig().get().isEnableRandomization();
        
        plugin.getAnnouncementConfig().load();
        
        int newInterval = plugin.getAnnouncementConfig().get().getIntervalSeconds();
        boolean newRandomization = plugin.getAnnouncementConfig().get().isEnableRandomization();
        
        boolean configChanged = (oldInterval != newInterval || oldRandomization != newRandomization);
        
        // Re-scan messages directory so new .json files are registered before reload
        plugin.discoverAndRegisterNewMessageConfigs();
        
        // Reload messages
        MessageLoader.reloadMessages();
        
        // Restart scheduler if config changed or if it needs to be restarted
        if (configChanged || AnnouncementScheduler.isRunning()) {
            AnnouncementScheduler.restart();
        }
        
        if(configChanged){
            context.sendMessage(Message.raw("Configuration and messages reloaded with new values!").color(Color.GREEN));
            return;
        }

        context.sendMessage(Message.raw("Configuration and messages reloaded.").color(Color.GREEN));
    }
}
