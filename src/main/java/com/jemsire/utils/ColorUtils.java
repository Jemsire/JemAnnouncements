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
    
    // Pattern for &x&R&R&G&G&B&B format
    private static final Pattern HEX_LEGACY_PATTERN = Pattern.compile("&x(&[0-9A-Fa-f]){6}");
    
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
     * Converts legacy & color codes to TinyMsg tags for backward compatibility
     * @param text The text with legacy & codes
     * @return The text with TinyMsg tags
     */
    public static String convertLegacyColorCodes(String text) {
        if (text == null || !text.contains("&")) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        char[] chars = text.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 1 < chars.length) {
                char code = Character.toLowerCase(chars[i + 1]);

                if (CODE_TO_COLOR_NAME.containsKey(code)) {
                    result.append("<").append(CODE_TO_COLOR_NAME.get(code)).append(">");
                    i++; // Skip the code character
                } else if (CODE_TO_STYLE_NAME.containsKey(code)) {
                    String style = CODE_TO_STYLE_NAME.get(code);
                    if (style.equals("reset")) {
                        result.append("<reset>");
                    } else if (style.equals("bold")) {
                        result.append("<bold>");
                    } else if (style.equals("underline")) {
                        result.append("<underline>");
                    } else if (style.equals("italic")) {
                        result.append("<italic>");
                    }
                    // Skip obfuscated and strikethrough as they're not directly supported
                    i++; // Skip the code character
                } else {
                    // Unknown code, keep as-is
                    result.append(chars[i]);
                }
            } else {
                result.append(chars[i]);
            }
        }

        return result.toString();
    }
    
    /**
     * Strips all color codes and formatting tags from text
     * Removes both TinyMsg tags and legacy & color codes
     * @param text The text to strip
     * @return The plain text without any formatting
     */
    public static String stripColorCodes(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        // Strip TinyMsg tags (<tag> and </tag>) - case insensitive
        String withoutTags = text.replaceAll("(?i)<[^>]+>", "");
        
        // Strip legacy & color codes (e.g., &a, &l, &r) - case insensitive
        // Matches & followed by 0-9, a-f, k, l, m, n, o, r
        return withoutTags.replaceAll("(?i)&[0-9a-fk-or]", "");
    }
}
