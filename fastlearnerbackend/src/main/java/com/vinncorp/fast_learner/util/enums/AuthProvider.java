package com.vinncorp.fast_learner.util.enums;

public enum AuthProvider {
    LOCAL(1), GOOGLE(2), FACEBOOK(3), LINKEDIN(4), APPLE(5);

    private int value;

    AuthProvider(int value) {
        this.value = value;
    }

    public int getValue(){ return value;}

    public static AuthProvider fromValue(int value) {
        for (AuthProvider authProvider : AuthProvider.values()) {
            if (authProvider.value == value) {
                return authProvider;
            }
        }
        return null;
    }

    public static boolean isValidProvider(String provider) {
        try {
            AuthProvider.valueOf(provider.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
