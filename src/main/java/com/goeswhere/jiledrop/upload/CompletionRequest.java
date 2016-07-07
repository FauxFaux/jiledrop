package com.goeswhere.jiledrop.upload;

import com.goeswhere.jiledrop.types.FileId;
import org.hibernate.validator.constraints.NotEmpty;

public class CompletionRequest {
    @NotEmpty public String fileId;
    @NotEmpty public int totalChunks;
    @NotEmpty public String fileName;
}
