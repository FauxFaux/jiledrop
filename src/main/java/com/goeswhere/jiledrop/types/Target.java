package com.goeswhere.jiledrop.types;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.goeswhere.jiledrop.auth.PBKDF2;

import java.security.SecureRandom;
import java.util.regex.Pattern;

@JsonSerialize(using = FlatSerialiser.class)
public class Target implements Type {
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

    public static Target random() {
        return new Target(new String(PBKDF2.randomChars(LENGTH)));
    }

    @Override
    public String getValue() {
        return value;
    }
}
