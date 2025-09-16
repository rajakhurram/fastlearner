package com.vinncorp.fast_learner.util.enums;

public enum FileType {
    PROFILE_IMAGE(0), PREVIEW_THUMBNAIL(1), PREVIEW_VIDEO(2),
    VIDEO(3), TRANSCRIBE(4), ARTICLE(5), DOCS(6);

    private int value;

    FileType(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static FileType fromValue(int value) {
        for (FileType fileType : FileType.values()) {
            if (fileType.value == value) {
                return fileType;
            }
        }
        return null;
    }

    public static FileType fromString(String value) {
        for (FileType fileType : FileType.values()) {
            if(fileType.name().equals(value))
                return fileType;
        }
        return null;
    }
}
