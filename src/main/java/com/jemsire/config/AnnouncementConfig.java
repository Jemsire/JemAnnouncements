package com.jemsire.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

/**
 * Main configuration for the announcement plugin.
 * Controls timing, order, and general settings.
 */
public class AnnouncementConfig {
    private int intervalSeconds = 300; // Default: 5 minutes
    private boolean enableRandomization = false; // Default: sequential order (false = sequential, true = random)
    private boolean createExampleMessages = true; // Default: true
    private String logLevel = "INFO"; // Default: INFO (INFO, DEBUG, NONE)
    private int version = 1;

    public AnnouncementConfig() {
    }

    public static final BuilderCodec<AnnouncementConfig> CODEC =
            BuilderCodec.builder(AnnouncementConfig.class, AnnouncementConfig::new)
                    .append(
                            new KeyedCodec<Integer>("IntervalSeconds", Codec.INTEGER),
                            (config, value, info) -> config.intervalSeconds = value != null ? value : 300,
                            (config, info) -> config.intervalSeconds
                    )
                    .add()

                    .append(
                            new KeyedCodec<Boolean>("EnableRandomization", Codec.BOOLEAN),
                            (config, value, info) -> config.enableRandomization = value != null ? value : false,
                            (config, info) -> config.enableRandomization
                    )
                    .add()

                    .append(
                            new KeyedCodec<Boolean>("CreateExampleMessages", Codec.BOOLEAN),
                            (config, value, info) -> config.createExampleMessages = value != null ? value : true,
                            (config, info) -> config.createExampleMessages
                    )
                    .add()

                    .append(
                            new KeyedCodec<String>("LogLevel", Codec.STRING),
                            (config, value, info) -> config.logLevel = value != null ? value : "INFO",
                            (config, info) -> config.logLevel
                    )
                    .add()

                    .append(
                            new KeyedCodec<Integer>("Version", Codec.INTEGER),
                            (config, value, info) -> config.version = value != null ? value : 1,
                            (config, info) -> config.version
                    )
                    .add()

                    .build();

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public boolean isEnableRandomization() {
        return enableRandomization;
    }

    public boolean isCreateExampleMessages() {
        return createExampleMessages;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public int getVersion() {
        return version;
    }

    public boolean isSequential() {
        return !enableRandomization;
    }

    public boolean isRandom() {
        return enableRandomization;
    }
}
