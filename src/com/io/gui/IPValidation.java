package com.io.gui;

import java.util.Arrays;

public class IPValidation {

    private static boolean isNumeric(String string) {
        for(char c : string.toCharArray()) {
            if(c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }

    public static boolean isIp(String string) {
        String[] parts = string.split("\\.", -1);
        return parts.length == 4 // 4 parts
                && Arrays.stream(parts)
                .filter(IPValidation::isNumeric) // Only numbers
                .map(Integer::parseInt)
                .filter(i -> i <= 255 && i >= 0) // Must be inside [0, 255]
                .count() == 4; // 4 numerical parts inside [0, 255]
    }
}
