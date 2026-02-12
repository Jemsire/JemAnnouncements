package com.jemsire.expansion;

import com.jemsire.jemplaceholders.api.PlaceholderExpansion;
import com.jemsire.plugin.AnnouncementPlugin;

/**
 * JemAnnouncement placeholders.
 */
public class JemAnnouncementsExpansion extends PlaceholderExpansion {

    @Override public String getIdentifier() { return "jemannouncements"; }

    @Override public String getName() {
        return "JemAnnouncements";
    }

    @Override public String getPluginNamespace() {
        return "Jemsire:JemAnnouncements";
    }

    @Override public String getDescription() {
        return "Provides placeholders for JemAnnouncements information.";
    }

    @Override public String getAuthor() { return "Jemsire"; }

    @Override public String getVersion() { return "1.0.0"; }

    @Override public String getWebsite() { return "https://www.curseforge.com/members/jemsire/projects"; }

    public JemAnnouncementsExpansion() {

        exact("size", (player, params) ->
                AnnouncementPlugin.get().getMessageConfigs().size() + ""
        );

        exact("interval", (player, params) ->
                AnnouncementPlugin.get().getAnnouncementConfig().get().getIntervalSeconds() + " Seconds."
        );

        exact("randomize", (player, params) ->
                AnnouncementPlugin.get().getAnnouncementConfig().get().isEnableRandomization() + ""
        );
    }
}
