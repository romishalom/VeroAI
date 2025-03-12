package com.rando.factchecker2.helpers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkFinder {
    public static String findLink(String text) {
        String regex = "\\b((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }
}
