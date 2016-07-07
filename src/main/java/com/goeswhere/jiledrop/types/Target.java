package com.goeswhere.jiledrop.types;

import java.util.regex.Pattern;

public class Target {
    public static final int LENGTH = 36;
    public static final String VALID_TARGET_REGEX = "[a-z]{" + LENGTH + "}";
    public static final Pattern VALID_TARGET = Pattern.compile(VALID_TARGET_REGEX);

    public final String value;

    public Target(String value) {
        if (!VALID_TARGET.matcher(value).matches()) {
            throw new IllegalStateException("invalid target: " + value);
        }
        this.value = value;
    }
}
