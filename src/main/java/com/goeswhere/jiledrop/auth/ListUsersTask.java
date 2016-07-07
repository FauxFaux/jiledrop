package com.goeswhere.jiledrop.auth;

import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.servlets.tasks.Task;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.io.PrintWriter;
import java.util.Map;

public class ListUsersTask extends Task {
    private final DBI jdbi;

    public ListUsersTask(DBI jdbi) {
        super("list-users");
        this.jdbi = jdbi;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        try (final Handle h = jdbi.open()) {
            for (Map<String, Object> row :
                    h.createQuery("select username, target from users order by username").list()) {
                output.println(row.get("target") + " " + row.get("username"));
            }
        }
    }
}
