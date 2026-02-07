package com.jemsire.config;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents a single announcement message with support for multiple message types.
 * Can contain chat messages, notifications, titles, and sounds.
 */
public class AnnouncementMessage {
    private String[] chatMessages = new String[0]; // Chat messages array (sent in order)
    private boolean centerChat = true; // Whether to center chat messages (default: true)
    private NotificationConfig notification = null; // Notification configuration (title, subtitle, icon)
    private TitleConfig title = null; // Title/subtitle configuration
    private SoundConfig sound = null; // Sound configuration
    private int priority = 0; // Higher priority messages are shown first (optional)
    private boolean enabled = true; // Whether this message is enabled

    public AnnouncementMessage() {
    }

    public static final BuilderCodec<AnnouncementMessage> CODEC =
            BuilderCodec.builder(AnnouncementMessage.class, AnnouncementMessage::new)
                    .append(
                            new KeyedCodec<String[]>("ChatMessages",
                                    new ArrayCodec<>(Codec.STRING, String[]::new)),
                            (config, value, info) -> {
                                if (value != null) {
                                    config.chatMessages = value;
                                }
                            },
                            (config, info) -> config.chatMessages
                    )
                    .add()

                    .append(
                            new KeyedCodec<NotificationConfig>("Notification", NotificationConfig.CODEC),
                            (config, value, info) -> config.notification = value,
                            (config, info) -> config.notification
                    )
                    .add()

                    .append(
                            new KeyedCodec<TitleConfig>("Title", TitleConfig.CODEC),
                            (config, value, info) -> config.title = value,
                            (config, info) -> config.title
                    )
                    .add()

                    .append(
                            new KeyedCodec<SoundConfig>("Sound", SoundConfig.CODEC),
                            (config, value, info) -> config.sound = value,
                            (config, info) -> config.sound
                    )
                    .add()

                    .append(
                            new KeyedCodec<Integer>("Priority", Codec.INTEGER),
                            (config, value, info) -> config.priority = value != null ? value : 0,
                            (config, info) -> config.priority
                    )
                    .add()

                    .append(
                            new KeyedCodec<Boolean>("Center", Codec.BOOLEAN),
                            (config, value, info) -> config.centerChat = value != null ? value : true,
                            (config, info) -> config.centerChat
                    )
                    .add()

                    .append(
                            new KeyedCodec<Boolean>("Enabled", Codec.BOOLEAN),
                            (config, value, info) -> config.enabled = value != null ? value : true,
                            (config, info) -> config.enabled
                    )
                    .add()

                    .build();

    /**
     * Parses an announcement message from JSON. Used for loading new message files at reload
     * without calling withConfig() (which must run before setup).
     */
    public static AnnouncementMessage fromJson(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(json.trim()).getAsJsonObject();
            AnnouncementMessage msg = new AnnouncementMessage();
            if (root.has("ChatMessages") && root.get("ChatMessages").isJsonArray()) {
                JsonArray arr = root.getAsJsonArray("ChatMessages");
                List<String> list = new ArrayList<>();
                for (JsonElement e : arr) {
                    list.add(e.isJsonNull() ? "" : e.getAsString());
                }
                msg.chatMessages = list.toArray(new String[0]);
            }
            msg.centerChat = !root.has("Center") || root.get("Center").isJsonNull() || root.get("Center").getAsBoolean();
            msg.priority = root.has("Priority") && !root.get("Priority").isJsonNull() ? root.get("Priority").getAsInt() : 0;
            msg.enabled = !root.has("Enabled") || root.get("Enabled").isJsonNull() || root.get("Enabled").getAsBoolean();
            if (root.has("Notification") && root.get("Notification").isJsonObject()) {
                JsonObject n = root.getAsJsonObject("Notification");
                String title = n.has("Title") && !n.get("Title").isJsonNull() ? n.get("Title").getAsString() : "";
                String subtitle = n.has("Subtitle") && !n.get("Subtitle").isJsonNull() ? n.get("Subtitle").getAsString() : "";
                String icon = n.has("Icon") && !n.get("Icon").isJsonNull() ? n.get("Icon").getAsString() : null;
                msg.notification = new NotificationConfig(title, subtitle, icon);
                com.jemsire.utils.Logger.debug("Manually parsed notification: title=" + title + ", subtitle=" + subtitle);
            }
            if (root.has("Title") && root.get("Title").isJsonObject()) {
                JsonObject t = root.getAsJsonObject("Title");
                String title = t.has("Title") && !t.get("Title").isJsonNull() ? t.get("Title").getAsString() : "";
                String subtitle = t.has("Subtitle") && !t.get("Subtitle").isJsonNull() ? t.get("Subtitle").getAsString() : "";
                boolean isMajor = t.has("IsMajor") && !t.get("IsMajor").isJsonNull() && t.get("IsMajor").getAsBoolean();
                float fadeIn = t.has("FadeIn") && !t.get("FadeIn").isJsonNull() ? t.get("FadeIn").getAsFloat() : 0.25f;
                float stay = t.has("Stay") && !t.get("Stay").isJsonNull() ? t.get("Stay").getAsFloat() : 5.0f;
                float fadeOut = t.has("FadeOut") && !t.get("FadeOut").isJsonNull() ? t.get("FadeOut").getAsFloat() : 0.25f;
                msg.title = new TitleConfig(title, subtitle, isMajor, fadeIn, stay, fadeOut);
                com.jemsire.utils.Logger.debug("Manually parsed title: title=" + title + ", subtitle=" + subtitle);
            }
            if (root.has("Sound") && root.get("Sound").isJsonObject()) {
                JsonObject s = root.getAsJsonObject("Sound");
                String soundName = s.has("SoundName") && !s.get("SoundName").isJsonNull() ? s.get("SoundName").getAsString() : "";
                float volume = s.has("Volume") && !s.get("Volume").isJsonNull() ? s.get("Volume").getAsFloat() : 1.0f;
                float pitch = s.has("Pitch") && !s.get("Pitch").isJsonNull() ? s.get("Pitch").getAsFloat() : 1.0f;
                msg.sound = new SoundConfig(soundName, volume, pitch);
            }
            return msg;
        } catch (Exception e) {
            return null;
        }
    }

    public String[] getChatMessages() {
        return chatMessages;
    }

    public NotificationConfig notification() {
        return notification;
    }

    public TitleConfig title() {
        return title;
    }

    public SoundConfig sound() {
        return sound;
    }

    public int getPriority() {
        return priority;
    }

    public boolean hasChatMessages() {
        return chatMessages != null && chatMessages.length > 0;
    }

    public boolean hasNotification() {
        return notification != null;
    }

    public boolean hasTitle() {
        return title != null;
    }

    public boolean hasSound() {
        return sound != null;
    }

    public boolean isCenterChat() {
        return centerChat;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Notification configuration for notification messages (similar to item pickup notifications)
     */
    public static class NotificationConfig {
        private String title;
        private String subtitle;
        private String icon;

        public NotificationConfig() {}

        public NotificationConfig(String title, String subtitle, String icon) {
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
        }

        public static final BuilderCodec<NotificationConfig> CODEC =
                BuilderCodec.builder(NotificationConfig.class, NotificationConfig::new)
                        .append(
                                new KeyedCodec<>("Title", Codec.STRING),
                                (config, value, info) -> {
                                    config.title = value;
                                    com.jemsire.utils.Logger.debug("NotificationCodec: title=" + value);
                                },
                                (config, info) -> config.title
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Subtitle", Codec.STRING),
                                (config, value, info) -> {
                                    config.subtitle = value;
                                    com.jemsire.utils.Logger.debug("NotificationCodec: subtitle=" + value);
                                },
                                (config, info) -> config.subtitle
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Icon", Codec.STRING),
                                (config, value, info) -> config.icon = value,
                                (config, info) -> config.icon
                        )
                        .add()

                        .build();

        public String title() { return title; }
        public String subtitle() { return subtitle; }
        public String icon() { return icon; }

        public boolean hasIcon() {
            return icon != null && !icon.isEmpty();
        }
    }

    /**
     * Title configuration for title/subtitle messages
     */
    public static class TitleConfig {
        private String title;
        private String subtitle;
        private boolean isMajor;
        private float fadeIn;
        private float stay;
        private float fadeOut;

        public TitleConfig() {}

        public TitleConfig(String title, String subtitle, boolean isMajor, float fadeIn, float stay, float fadeOut) {
            this.title = title;
            this.subtitle = subtitle;
            this.isMajor = isMajor;
            this.fadeIn = fadeIn;
            this.stay = stay;
            this.fadeOut = fadeOut;
        }

        public static final BuilderCodec<TitleConfig> CODEC =
                BuilderCodec.builder(TitleConfig.class, TitleConfig::new)
                        .append(
                                new KeyedCodec<>("Title", Codec.STRING),
                                (config, value, info) -> {
                                    config.title = value;
                                    com.jemsire.utils.Logger.debug("TitleCodec: title=" + value);
                                },
                                (config, info) -> config.title
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Subtitle", Codec.STRING),
                                (config, value, info) -> {
                                    config.subtitle = value;
                                    com.jemsire.utils.Logger.debug("TitleCodec: subtitle=" + value);
                                },
                                (config, info) -> config.subtitle
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("IsMajor", Codec.BOOLEAN),
                                (config, value, info) -> config.isMajor = value != null ? value : false,
                                (config, info) -> config.isMajor
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("FadeIn", Codec.FLOAT),
                                (config, value, info) -> config.fadeIn = value != null ? value : 0.25f,
                                (config, info) -> config.fadeIn
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Stay", Codec.FLOAT),
                                (config, value, info) -> config.stay = value != null ? value : 5.0f,
                                (config, info) -> config.stay
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("FadeOut", Codec.FLOAT),
                                (config, value, info) -> config.fadeOut = value != null ? value : 0.25f,
                                (config, info) -> config.fadeOut
                        )
                        .add()

                        .build();

        public String title() { return title; }
        public String subtitle() { return subtitle; }
        public boolean isMajor() { return isMajor; }
        public float fadeIn() { return fadeIn; }
        public float stay() { return stay; }
        public float fadeOut() { return fadeOut; }
    }

    /**
     * Sound configuration for playing sounds with announcements
     */
    public static class SoundConfig {
        private String soundName;
        private float volume;
        private float pitch;

        public SoundConfig() {}

        public SoundConfig(String soundName, float volume, float pitch) {
            this.soundName = soundName;
            this.volume = volume;
            this.pitch = pitch;
        }

        public static final BuilderCodec<SoundConfig> CODEC =
                BuilderCodec.builder(SoundConfig.class, SoundConfig::new)
                        .append(
                                new KeyedCodec<>("SoundName", Codec.STRING),
                                (config, value, info) -> config.soundName = value,
                                (config, info) -> config.soundName
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Volume", Codec.FLOAT),
                                (config, value, info) -> config.volume = value != null ? value : 1.0f,
                                (config, info) -> config.volume
                        )
                        .add()

                        .append(
                                new KeyedCodec<>("Pitch", Codec.FLOAT),
                                (config, value, info) -> config.pitch = value != null ? value : 1.0f,
                                (config, info) -> config.pitch
                        )
                        .add()

                        .build();

        public String soundName() { return soundName; }
        public float volume() { return volume; }
        public float pitch() { return pitch; }
    }
}
