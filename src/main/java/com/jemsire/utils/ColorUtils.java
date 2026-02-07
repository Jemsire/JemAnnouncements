package com.jemsire.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling color codes in messages.
 * Supports both & color codes (like &a, &c) and hex colors (like #FF0000 or &x&F&F&0&0&0&0).
 */
public class ColorUtils {
    private ColorUtils() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    // Minecraft/Hytale color code mapping
    private static final Map<Character, Color> COLOR_CODES = new HashMap<>();
    
    static {
        COLOR_CODES.put('0', new Color(0, 0, 0));       // Black
        COLOR_CODES.put('1', new Color(0, 0, 170));      // Dark Blue
        COLOR_CODES.put('2', new Color(0, 170, 0));      // Dark Green
        COLOR_CODES.put('3', new Color(0, 170, 170));    // Dark Aqua
        COLOR_CODES.put('4', new Color(170, 0, 0));      // Dark Red
        COLOR_CODES.put('5', new Color(170, 0, 170));    // Dark Purple
        COLOR_CODES.put('6', new Color(255, 170, 0));    // Gold
        COLOR_CODES.put('7', new Color(170, 170, 170));  // Gray
        COLOR_CODES.put('8', new Color(85, 85, 85));     // Dark Gray
        COLOR_CODES.put('9', new Color(85, 85, 255));    // Blue
        COLOR_CODES.put('a', new Color(85, 255, 85));     // Green
        COLOR_CODES.put('b', new Color(85, 255, 255));   // Aqua
        COLOR_CODES.put('c', new Color(255, 85, 85));    // Red
        COLOR_CODES.put('d', new Color(255, 85, 255));   // Light Purple
        COLOR_CODES.put('e', new Color(255, 255, 85));   // Yellow
        COLOR_CODES.put('f', new Color(255, 255, 255));  // White
    }
    
    // Format codes
    private static final Map<Character, String> FORMAT_CODES = new HashMap<>();
    
    static {
        FORMAT_CODES.put('k', "obfuscated");
        FORMAT_CODES.put('l', "bold");
        FORMAT_CODES.put('m', "strikethrough");
        FORMAT_CODES.put('n', "underline");
        FORMAT_CODES.put('o', "italic");
        FORMAT_CODES.put('r', "reset");
    }
    
    // Named colors map (for TinyMsg compatibility)
    private static final Map<String, Color> NAMED_COLORS = new HashMap<>();
    
    static {
        NAMED_COLORS.put("black", new Color(0, 0, 0));
        NAMED_COLORS.put("dark_blue", new Color(0, 0, 170));
        NAMED_COLORS.put("dark_green", new Color(0, 170, 0));
        NAMED_COLORS.put("dark_aqua", new Color(0, 170, 170));
        NAMED_COLORS.put("dark_red", new Color(170, 0, 0));
        NAMED_COLORS.put("dark_purple", new Color(170, 0, 170));
        NAMED_COLORS.put("gold", new Color(255, 170, 0));
        NAMED_COLORS.put("gray", new Color(170, 170, 170));
        NAMED_COLORS.put("dark_gray", new Color(85, 85, 85));
        NAMED_COLORS.put("blue", new Color(85, 85, 255));
        NAMED_COLORS.put("green", new Color(85, 255, 85));
        NAMED_COLORS.put("aqua", new Color(85, 255, 255));
        NAMED_COLORS.put("red", new Color(255, 85, 85));
        NAMED_COLORS.put("light_purple", new Color(255, 85, 255));
        NAMED_COLORS.put("yellow", new Color(255, 255, 85));
        NAMED_COLORS.put("white", new Color(255, 255, 255));
    }
    
    // Color code to name mapping (for legacy code conversion)
    private static final Map<Character, String> CODE_TO_COLOR_NAME = new HashMap<>();
    
    static {
        CODE_TO_COLOR_NAME.put('0', "black");
        CODE_TO_COLOR_NAME.put('1', "dark_blue");
        CODE_TO_COLOR_NAME.put('2', "dark_green");
        CODE_TO_COLOR_NAME.put('3', "dark_aqua");
        CODE_TO_COLOR_NAME.put('4', "dark_red");
        CODE_TO_COLOR_NAME.put('5', "dark_purple");
        CODE_TO_COLOR_NAME.put('6', "gold");
        CODE_TO_COLOR_NAME.put('7', "gray");
        CODE_TO_COLOR_NAME.put('8', "dark_gray");
        CODE_TO_COLOR_NAME.put('9', "blue");
        CODE_TO_COLOR_NAME.put('a', "green");
        CODE_TO_COLOR_NAME.put('b', "aqua");
        CODE_TO_COLOR_NAME.put('c', "red");
        CODE_TO_COLOR_NAME.put('d', "light_purple");
        CODE_TO_COLOR_NAME.put('e', "yellow");
        CODE_TO_COLOR_NAME.put('f', "white");
    }
    
    // Format code to style name mapping (for legacy code conversion)
    private static final Map<Character, String> CODE_TO_STYLE_NAME = new HashMap<>();
    
    static {
        CODE_TO_STYLE_NAME.put('k', "obfuscated");
        CODE_TO_STYLE_NAME.put('l', "bold");
        CODE_TO_STYLE_NAME.put('m', "strikethrough");
        CODE_TO_STYLE_NAME.put('n', "underline");
        CODE_TO_STYLE_NAME.put('o', "italic");
        CODE_TO_STYLE_NAME.put('r', "reset");
    }
    
    // Pattern for hex colors: #RRGGBB or #RGB
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})|#([0-9A-Fa-f]{6})|#([0-9A-Fa-f]{3})");

    // Pattern for <offset:N> centering adjustment (e.g. <offset:5> add spaces, <offset:-2> remove spaces)
    private static final Pattern OFFSET_TAG_PATTERN = Pattern.compile("<offset:(-?\\d+)>", Pattern.CASE_INSENSITIVE);
    
    /**
     * Converts a string with color codes to a Color object.
     * Supports & codes (like &a, &c) and hex colors (like #FF0000).
     * 
     * @param colorString The color string (e.g., "&a", "#FF0000", "&x&F&F&0&0&0&0")
     * @return The Color object, or null if invalid
     */
    public static Color parseColor(String colorString) {
        if (colorString == null || colorString.isEmpty()) {
            return null;
        }
        
        // Handle & code colors (single character after &)
        if (colorString.startsWith("&") && colorString.length() == 2) {
            char code = colorString.charAt(1);
            if (COLOR_CODES.containsKey(code)) {
                return COLOR_CODES.get(code);
            }
        }
        
        // Handle hex colors: #RRGGBB or #RGB
        Matcher hexMatcher = HEX_PATTERN.matcher(colorString);
        if (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            if (hex == null) {
                hex = hexMatcher.group(2);
            }
            if (hex == null) {
                // Short hex format #RGB -> #RRGGBB
                String shortHex = hexMatcher.group(3);
                if (shortHex != null) {
                    hex = "" + shortHex.charAt(0) + shortHex.charAt(0) +
                          shortHex.charAt(1) + shortHex.charAt(1) +
                          shortHex.charAt(2) + shortHex.charAt(2);
                }
            }
            if (hex != null) {
                try {
                    return new Color(Integer.parseInt(hex, 16));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a character is a valid color code
     */
    public static boolean isColorCode(char code) {
        return COLOR_CODES.containsKey(Character.toLowerCase(code));
    }
    
    /**
     * Checks if a character is a valid format code
     */
    public static boolean isFormatCode(char code) {
        return FORMAT_CODES.containsKey(Character.toLowerCase(code));
    }
    
    /**
     * Gets the Color object for a color code character
     */
    public static Color getColorForCode(char code) {
        return COLOR_CODES.get(Character.toLowerCase(code));
    }
    
    /**
     * Gets a named color by its string name (e.g., "red", "blue", "dark_blue")
     * @param name The color name
     * @return The Color object, or null if not found
     */
    public static Color getNamedColor(String name) {
        if (name == null) {
            return null;
        }
        return NAMED_COLORS.get(name.toLowerCase());
    }
    
    /**
     * Checks if a named color exists
     * @param name The color name
     * @return true if the named color exists
     */
    public static boolean hasNamedColor(String name) {
        if (name == null) {
            return false;
        }
        return NAMED_COLORS.containsKey(name.toLowerCase());
    }
    
    /**
     * Parses a hex color string (with or without # prefix)
     * @param hex The hex color string (e.g., "#FF0000" or "FF0000")
     * @return The Color object, or null if invalid
     */
    public static Color parseHexColor(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        
        try {
            String clean = hex.replace("#", "");
            if (clean.length() == 6) {
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                return new Color(r, g, b);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Parses a color from a string argument (named color or hex)
     * @param arg The color argument (e.g., "red", "#FF0000", "FF0000")
     * @return The Color object, or null if invalid
     */
    public static Color parseColorArg(String arg) {
        if (arg == null || arg.isEmpty()) {
            return null;
        }
        
        // Try named color first
        Color namedColor = getNamedColor(arg);
        if (namedColor != null) {
            return namedColor;
        }
        
        // Try hex color
        return parseHexColor(arg);
    }
    
    /**
     * Converts legacy & color codes to TinyMsg tags for backward compatibility.
     * Outputs tags compatible with TinyMessage API: &lt;color:name&gt;, &lt;b&gt;, &lt;i&gt;, &lt;u&gt;, &lt;reset&gt;, &lt;color:#RRGGBB&gt; for hex.
     * @param text The text with legacy & codes (e.g. &a, &l, &#RRGGBB, &x&R&G&B)
     * @return The text with TinyMsg tags for TinyMsg.parse()
     */
    public static String convertLegacyColorCodes(String text) {
        if (text == null) {
            return text;
        }
        // First convert hex patterns so we don't confuse them with single-char codes
        String s = text;
        // &#RRGGBB -> <color:#RRGGBB>
        s = s.replaceAll("&#([0-9A-Fa-f]{6})", "<color:#$1>");
        // &x&R&R&G&G&B&B -> <color:#RRGGBB>
        Matcher hexLegacy = Pattern.compile("&x(&[0-9A-Fa-f]){6}").matcher(s);
        StringBuffer hexRepl = new StringBuffer();
        while (hexLegacy.find()) {
            String match = hexLegacy.group();
            // match is "&x&R&R&G&G&B&B" - hex digits at indices 3,5,7,9,11,13
            String hex = "" + match.charAt(3) + match.charAt(5) + match.charAt(7) + match.charAt(9) + match.charAt(11) + match.charAt(13);
            hexLegacy.appendReplacement(hexRepl, Matcher.quoteReplacement("<color:#" + hex + ">"));
        }
        hexLegacy.appendTail(hexRepl);
        s = hexRepl.toString();

        if (!s.contains("&")) {
            return s;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);

                if (CODE_TO_COLOR_NAME.containsKey(code)) {
                    result.append("<color:").append(CODE_TO_COLOR_NAME.get(code)).append(">");
                    i++;
                } else if (CODE_TO_STYLE_NAME.containsKey(code)) {
                    String style = CODE_TO_STYLE_NAME.get(code);
                    switch (style) {
                        case "reset" -> result.append("<reset>");
                        case "bold" -> result.append("<b>");
                        case "underline" -> result.append("<u>");
                        case "italic" -> result.append("<i>");
                    }
                    i++;
                } else {
                    result.append(chars[i]);
                }
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }
    
    /**
     * Returns the offset value from the first &lt;offset:N&gt; tag in the text. One offset per message line:
     * positive = add more leading spaces (shift right), negative = fewer spaces (shift left).
     * Additional offset tags on the same line are ignored; each chat line gets its own single offset.
     * @param text The text that may contain one &lt;offset:N&gt; tag
     * @return The N value, or 0 if none or invalid
     */
    public static int getOffset(String text) {
        if (text == null || !text.contains("offset")) {
            return 0;
        }
        Matcher m = OFFSET_TAG_PATTERN.matcher(text);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    /**
     * Removes all &lt;offset:N&gt; tags from text. Call before centering and before TinyMsg.parse
     * so the tags are not displayed and not counted as visible width.
     * @param text The text that may contain &lt;offset:N&gt; tags
     * @return The text with all &lt;offset:N&gt; tags removed
     */
    public static String stripOffsetTags(String text) {
        if (text == null) {
            return text;
        }
        return OFFSET_TAG_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Strips all color codes and formatting tags from text
     * Removes TinyMsg tags, legacy & codes, and hex (#RRGGBB, &#RRGGBB, &x&R&G&B)
     * @param text The text to strip
     * @return The plain text without any formatting
     */
    public static String stripColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        // Strip TinyMsg tags (<tag> and </tag>) - case insensitive
        String s = text.replaceAll("(?i)<[^>]+>", "");
        // Strip legacy & single-char codes (e.g., &a, &l, &r)
        s = s.replaceAll("(?i)&[0-9a-fk-or]", "");
        // Strip hex: &#RRGGBB and &x&R&R&G&G&B&B (legacy hex)
        s = s.replaceAll("&#[0-9A-Fa-f]{6}", "");
        s = s.replaceAll("&x(&[0-9A-Fa-f]){6}", "");
        // Strip standalone #RRGGBB and #RGB (so they are not counted as visible length)
        s = s.replaceAll("#[0-9A-Fa-f]{6}", "");
        s = s.replaceAll("#[0-9A-Fa-f]{3}", "");
        return s;
    }

    /**
     * Returns the visible character count for centering. Counts only visible characters;
     * tags and color codes are skipped. Used with center width 80: spaces = (80 - count) / 2.
     */
    public static int getVisibleWidthForCentering(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int width = 0;
        int i = 0;
        while (i < text.length()) {
            if (text.charAt(i) == '<') {
                int end = text.indexOf('>', i + 1);
                if (end == -1) {
                    width++;
                    i++;
                    continue;
                }
                i = end + 1;
                continue;
            }
            if (text.charAt(i) == '&' && i + 1 < text.length()) {
                char c = Character.toLowerCase(text.charAt(i + 1));
                boolean singleCode = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || c == 'k' || c == 'l' || c == 'm' || c == 'n' || c == 'o' || c == 'r';
                if (singleCode) {
                    i += 2;
                    continue;
                }
                if (c == '#' && i + 8 <= text.length()) {
                    String hex = text.substring(i + 2, i + 8);
                    if (hex.matches("[0-9A-Fa-f]{6}")) {
                        i += 8;
                        continue;
                    }
                }
                if (c == 'x' && i + 12 <= text.length()) {
                    String rest = text.substring(i, i + 12);
                    if (rest.matches("(?i)&x(&[0-9a-f]){6}")) {
                        i += 12;
                        continue;
                    }
                }
            }
            if (text.charAt(i) == '#') {
                int hexEnd = i + 1;
                while (hexEnd < text.length() && hexEnd - i <= 7 && (Character.isDigit(text.charAt(hexEnd)) || "abcdefABCDEF".indexOf(text.charAt(hexEnd)) >= 0)) {
                    hexEnd++;
                }
                if (hexEnd - i == 7 || hexEnd - i == 4) {
                    i = hexEnd;
                    continue;
                }
            }
            width++;
            i++;
        }
        return width;
    }
}
