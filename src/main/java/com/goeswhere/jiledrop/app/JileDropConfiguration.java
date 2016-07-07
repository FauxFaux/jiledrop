package com.goeswhere.jiledrop.app;

import io.dropwizard.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class JileDropConfiguration extends Configuration {
    private static final Logger logger = LoggerFactory.getLogger(JileDropConfiguration.class);

    private File storageDirectory;

    public File getStorageDirectory() {
        return storageDirectory;
    }

    public void setStorageDirectory(File storageDirectory) {
        File candidate;
        if (null != storageDirectory) {
            candidate = storageDirectory;
        } else {
            candidate = new File(".");
        }

        candidate = candidate.getAbsoluteFile();
        if (!candidate.isDirectory() || !candidate.canWrite()) {
            throw new IllegalStateException("can't write to storage directory: " + candidate);
        }

        logger.info("picked storage directory: {}", candidate);

        this.storageDirectory = candidate;
    }
}
