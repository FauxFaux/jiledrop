package com.goeswhere.jiledrop.types;

import java.util.regex.Pattern;

public class FileId {
    public static final int LENGTH = 10;
    public static final Pattern VALID_FILE_ID = Pattern.compile("^[a-zA-Z0-9]{" + LENGTH + "}$");

    public final String value;

    public FileId(String fileId) {
        if (!VALID_FILE_ID.matcher(fileId).matches()) {
            throw new IllegalStateException("invalid fileId: " + fileId);
        }
        this.value = fileId;
    }
}
