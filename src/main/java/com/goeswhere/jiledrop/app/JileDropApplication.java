package com.goeswhere.jiledrop.app;

import com.goeswhere.jiledrop.upload.UploadResource;
import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class JileDropApplication extends Application<JileDropConfiguration> {
    public static void main(String[] args) throws Exception {
        new JileDropApplication().run(args);
    }

    @Override
    public String getName() {
        return "jile-drop";
    }

    @Override
    public void initialize(Bootstrap<JileDropConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(JileDropConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(configuration).to(JileDropConfiguration.class);
            }
        });
        environment.jersey().register(UploadResource.class);
    }
}
