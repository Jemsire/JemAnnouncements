package com.jemsire.utils;

import com.hypixel.hytale.protocol.MaybeBool;
import com.hypixel.hytale.server.core.Message;

import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TinyMsg - Advanced message formatting parser for Hytale.
 * Supports colors, gradients, styles, and links using tag-based syntax.
 * 
 * Example usage:
 *   TinyMsg.parse("<red>Hello</red> <bold>World</bold>")
 *   TinyMsg.parse("<color:#FF0000>Red text</color>")
 *   TinyMsg.parse("<gradient:#FF0000:#00FF00>Gradient text</gradient>")
 */
public class TinyMsg {
    // Matches <tag>, <tag:arg>, </tag>
    private static final Pattern TAG_PATTERN = Pattern.compile("<(/?)([a-zA-Z0-9_]+)(?::([^>]+))?>");

    private record StyleState(
            Color color,
            List<Color> gradient,
            boolean bold,
            boolean italic,
            boolean underlined,
            boolean monospace,
            String link) {

        StyleState() {
            this(null, null, false, false, false, false, null);
        }

        StyleState copy() {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withColor(Color color) {
            return new StyleState(color, null, bold, italic, underlined, monospace, link);
        }

        StyleState withGradient(List<Color> gradient) {
            return new StyleState(null, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withBold(boolean bold) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withItalic(boolean italic) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withUnderlined(boolean underlined) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withMonospace(boolean monospace) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }

        StyleState withLink(String link) {
            return new StyleState(color, gradient, bold, italic, underlined, monospace, link);
        }
    }

    /**
     * Parses a string containing TinyMsg formatting tags and converts it into a Hytale Message.
     * <p>
     * This method processes all supported tags including colors, gradients, styles, and links.
     * Tags can be nested indefinitely for complex formatting.
     * </p>
     *
     * @param text The string to parse, containing TinyMsg formatting tags
     * @return A formatted {@link Message} object ready to be sent to players
     * @throws NullPointerException if text is null
     * @see Message
     */
    public static Message parse(String text) {
        if (text == null) {
            return Message.raw("");
        }
        
        if (!text.contains("<")) {
            return Message.raw(text);
        }

        Message root = Message.empty();

        // Stack keeps track of nested styles.
        // Example: Stack = [Base, Bold, Bold+Red]
        Deque<StyleState> stateStack = new ArrayDeque<>();
        stateStack.push(new StyleState()); // Start with default empty state

        Matcher matcher = TAG_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();

            // Handle text BEFORE this tag (using the state at the top of the stack)
            if (start > lastIndex) {
                String content = text.substring(lastIndex, start);
                Message segmentMsg = createStyledMessage(content, stateStack.peek());
                root.insert(segmentMsg);
            }

            // Process the tag to update the Stack
            boolean isClosing = "/".equals(matcher.group(1));
            String tagName = matcher.group(2).toLowerCase();
            String tagArg = matcher.group(3);

            if (isClosing) {
                if (stateStack.size() > 1) {
                    stateStack.pop();
                }
            } else {
                // Start with the current state, and modify it
                StyleState currentState = stateStack.peek();
                StyleState newState = currentState.copy();

                // If checking named colors directly
                if (ColorUtils.hasNamedColor(tagName)) {
                    newState = newState.withColor(ColorUtils.getNamedColor(tagName));
                } else {
                    switch (tagName) {
                        case "color":
                        case "c":
                        case "colour":
                            Color c = ColorUtils.parseColorArg(tagArg);
                            if (c != null) newState = newState.withColor(c);
                            break;

                        case "grnt":
                        case "gradient":
                            if (tagArg != null) {
                                List<Color> colors = parseGradientColors(tagArg);
                                if (!colors.isEmpty()) {
                                    newState = newState.withGradient(colors);
                                }
                            }
                            break;

                        case "bold":
                        case "b":
                            newState = newState.withBold(true);
                            break;

                        case "italic":
                        case "i":
                        case "em":
                            newState = newState.withItalic(true);
                            break;

                        case "underline":
                        case "u":
                            newState = newState.withUnderlined(true);
                            break;

                        case "monospace":
                        case "mono":
                            newState = newState.withMonospace(true);
                            break;

                        case "link":
                        case "url":
                            if (tagArg != null) newState = newState.withLink(tagArg);
                            break;

                        case "reset":
                        case "r":
                            stateStack.clear();
                            newState = new StyleState();
                            break;
                    }
                }
                stateStack.push(newState);
            }

            lastIndex = end;
        }

        if (lastIndex < text.length()) {
            String content = text.substring(lastIndex);
            Message segmentMsg = createStyledMessage(content, stateStack.peek());
            root.insert(segmentMsg);
        }

        return root;
    }

    private static Message createStyledMessage(String content, StyleState state) {
        // If we have a gradient, we must return a container with char-by-char coloring
        if (state.gradient != null && !state.gradient.isEmpty()) {
            return applyGradient(content, state);
        }

        Message msg = Message.raw(content);

        if (state.color != null) msg = msg.color(state.color);
        if (state.bold) msg = msg.bold(true);
        if (state.italic) msg = msg.italic(true);
        if (state.monospace) msg = msg.monospace(true);
        if (state.underlined) msg.getFormattedMessage().underlined = MaybeBool.True;
        if (state.link != null) msg = msg.link(state.link);

        return msg;
    }

    private static Message applyGradient(String text, StyleState state) {
        Message container = Message.empty();
        List<Color> colors = state.gradient;
        int length = text.length();

        for (int index = 0; index < length; index++) {
            char ch = text.charAt(index);
            float progress = index / (float) Math.max(length - 1, 1);
            Color color = interpolateColor(colors, progress);

            Message charMsg = Message.raw(String.valueOf(ch)).color(color);

            if (state.bold) charMsg = charMsg.bold(true);
            if (state.italic) charMsg = charMsg.italic(true);
            if (state.monospace) charMsg = charMsg.monospace(true);
            if (state.underlined) charMsg.getFormattedMessage().underlined = MaybeBool.True;
            if (state.link != null) charMsg = charMsg.link(state.link);

            container.insert(charMsg);
        }
        return container;
    }

    private static List<Color> parseGradientColors(String arg) {
        List<Color> colors = new ArrayList<>();
        for (String part : arg.split(":")) {
            Color c = ColorUtils.parseColorArg(part);
            if (c != null) colors.add(c);
        }
        return colors;
    }

    private static Color interpolateColor(List<Color> colors, float progress) {
        float clampedProgress = Math.max(0f, Math.min(1f, progress));
        float scaledProgress = clampedProgress * (colors.size() - 1);
        int index = Math.min((int) scaledProgress, colors.size() - 2);
        float localProgress = scaledProgress - index;

        Color c1 = colors.get(index);
        Color c2 = colors.get(index + 1);

        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * localProgress);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * localProgress);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * localProgress);

        return new Color(r, g, b);
    }
}
