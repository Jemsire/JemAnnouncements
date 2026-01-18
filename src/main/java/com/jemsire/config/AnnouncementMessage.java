package com.jemsire.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;


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

    public String[] getChatMessages() {
        return chatMessages;
    }

    public NotificationConfig getNotification() {
        return notification;
    }

    public TitleConfig getTitle() {
        return title;
    }

    public SoundConfig getSound() {
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
        private String title = ""; // Primary message
        private String subtitle = ""; // Secondary message (optional)
        private String icon = null; // Item icon (optional, e.g., "Weapon_Sword_Mithril")

        public NotificationConfig() {
        }

        public static final BuilderCodec<NotificationConfig> CODEC =
                BuilderCodec.builder(NotificationConfig.class, NotificationConfig::new)
                        .append(
                                new KeyedCodec<String>("Title", Codec.STRING),
                                (config, value, info) -> config.title = value != null ? value : "",
                                (config, info) -> config.title
                        )
                        .add()

                        .append(
                                new KeyedCodec<String>("Subtitle", Codec.STRING),
                                (config, value, info) -> config.subtitle = value != null ? value : "",
                                (config, info) -> config.subtitle
                        )
                        .add()

                        .append(
                                new KeyedCodec<String>("Icon", Codec.STRING),
                                (config, value, info) -> config.icon = value,
                                (config, info) -> config.icon
                        )
                        .add()

                        .build();

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public String getIcon() {
            return icon;
        }

        public boolean hasIcon() {
            return icon != null && !icon.isEmpty();
        }
    }

    /**
     * Title configuration for title/subtitle messages
     */
    public static class TitleConfig {
        private String title = "";
        private String subtitle = "";
        private boolean isMajor = false; // If true, adds a gold border around the title
        private float fadeIn = 0.25f; // Seconds
        private float stay = 5.0f; // Seconds
        private float fadeOut = 0.25f; // Seconds

        public TitleConfig() {
        }

        public static final BuilderCodec<TitleConfig> CODEC =
                BuilderCodec.builder(TitleConfig.class, TitleConfig::new)
                        .append(
                                new KeyedCodec<String>("Title", Codec.STRING),
                                (config, value, info) -> config.title = value != null ? value : "",
                                (config, info) -> config.title
                        )
                        .add()

                        .append(
                                new KeyedCodec<String>("Subtitle", Codec.STRING),
                                (config, value, info) -> config.subtitle = value != null ? value : "",
                                (config, info) -> config.subtitle
                        )
                        .add()

                        .append(
                                new KeyedCodec<Boolean>("IsMajor", Codec.BOOLEAN),
                                (config, value, info) -> config.isMajor = value != null ? value : false,
                                (config, info) -> config.isMajor
                        )
                        .add()

                        .append(
                                new KeyedCodec<Float>("FadeIn", Codec.FLOAT),
                                (config, value, info) -> config.fadeIn = value != null ? value : 0.25f,
                                (config, info) -> config.fadeIn
                        )
                        .add()

                        .append(
                                new KeyedCodec<Float>("Stay", Codec.FLOAT),
                                (config, value, info) -> config.stay = value != null ? value : 5.0f,
                                (config, info) -> config.stay
                        )
                        .add()

                        .append(
                                new KeyedCodec<Float>("FadeOut", Codec.FLOAT),
                                (config, value, info) -> config.fadeOut = value != null ? value : 0.25f,
                                (config, info) -> config.fadeOut
                        )
                        .add()

                        .build();

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public boolean isMajor() {
            return isMajor;
        }

        public float getFadeIn() {
            return fadeIn;
        }

        public float getStay() {
            return stay;
        }

        public float getFadeOut() {
            return fadeOut;
        }
    }

    /**
     * Sound configuration for playing sounds with announcements
     */
    public static class SoundConfig {
        private String soundName = "";
        private float volume = 1.0f;
        private float pitch = 1.0f;

        public SoundConfig() {
        }

        public static final BuilderCodec<SoundConfig> CODEC =
                BuilderCodec.builder(SoundConfig.class, SoundConfig::new)
                        .append(
                                new KeyedCodec<String>("SoundName", Codec.STRING),
                                (config, value, info) -> config.soundName = value != null ? value : "",
                                (config, info) -> config.soundName
                        )
                        .add()

                        .append(
                                new KeyedCodec<Float>("Volume", Codec.FLOAT),
                                (config, value, info) -> config.volume = value != null ? value : 1.0f,
                                (config, info) -> config.volume
                        )
                        .add()

                        .append(
                                new KeyedCodec<Float>("Pitch", Codec.FLOAT),
                                (config, value, info) -> config.pitch = value != null ? value : 1.0f,
                                (config, info) -> config.pitch
                        )
                        .add()

                        .build();

        public String getSoundName() {
            return soundName;
        }

        public float getVolume() {
            return volume;
        }

        public float getPitch() {
            return pitch;
        }
    }
}
