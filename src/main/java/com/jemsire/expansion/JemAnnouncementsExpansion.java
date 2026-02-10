package com.jemsire.expansion;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.jemsire.jemplaceholders.api.PlaceholderExpansion;
import com.jemsire.plugin.AnnouncementPlugin;

/**
 * JemLives placeholders.
 */
public class JemAnnouncementsExpansion extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "jemannouncements";
    }

    @Override
    public String getAuthor() {
        return "Jemsire";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String getName() {
        return "JemAnnouncements";
    }

    @Override
    public String getPluginNamespace() {
        return "Jemsire:JemAnnouncements";
    }

    @Override
    public String getDescription() {
        return "Provides placeholders for JemAnnouncements information.";
    }

    @Override
    public String onPlaceholderRequest(PlayerRef player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("size")) {

            return "" + AnnouncementPlugin.get().getMessageConfigs().size();
        }

        if (params.equalsIgnoreCase("interval")) {

            return AnnouncementPlugin.get().getAnnouncementConfig().get().getIntervalSeconds() + " Seconds.";
        }

        if (params.equalsIgnoreCase("randomize")) {

            return "" + AnnouncementPlugin.get().getAnnouncementConfig().get().isEnableRandomization();
        }

        return null;
    }
}
