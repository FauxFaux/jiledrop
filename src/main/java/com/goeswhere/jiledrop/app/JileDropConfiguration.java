package com.goeswhere.jiledrop.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }
}
