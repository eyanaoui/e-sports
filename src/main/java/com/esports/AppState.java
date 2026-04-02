package com.esports;

public class AppState {
    private static boolean darkMode = false;

    public static boolean isDarkMode() { return darkMode; }
    public static void setDarkMode(boolean dark) { darkMode = dark; }
}