package com.goeswhere.jiledrop.app;

import com.goeswhere.jiledrop.auth.AddUserTask;
import com.goeswhere.jiledrop.auth.AuthResource;
import com.goeswhere.jiledrop.auth.ListUsersTask;
import com.goeswhere.jiledrop.upload.UploadResource;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.skife.jdbi.v2.DBI;

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
        bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
        bootstrap.addBundle(new MigrationsBundle<JileDropConfiguration>() {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(JileDropConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(JileDropConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(configuration).to(JileDropConfiguration.class);
            }
        });
        environment.jersey().register(UploadResource.class);

        final DBIFactory factory = new DBIFactory();
        final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        environment.jersey().register(new AuthResource(jdbi));
        environment.admin().addTask(new ListUsersTask(jdbi));
        environment.admin().addTask(new AddUserTask(jdbi));
    }
}
