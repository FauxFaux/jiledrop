package com.goeswhere.jiledrop.auth;

import com.goeswhere.jiledrop.types.Target;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import io.dropwizard.servlets.tasks.Task;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.io.PrintWriter;

public class AddUserTask extends Task {
    private final DBI jdbi;

    public AddUserTask(DBI jdbi) {
        super("add-user");
        this.jdbi = jdbi;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        final String username = Iterables.getOnlyElement(parameters.get("username"));
        char[] password = parsePassword(parameters.get("password"));
        final String newTarget = Target.random().value;
        try (final Handle h = jdbi.open()) {
            if (1 != h.insert("insert into users (username, password, target) values (?,?,?)",
                    username,
                    PBKDF2.createHash(password),
                    newTarget)) {
                throw new IllegalStateException("couldn't insert");
            }
        }

        output.println(username);
        output.println(new String(password));
        output.println(newTarget);
    }

    private char[] parsePassword(ImmutableCollection<String> pass) {
        if (pass.isEmpty()) {
            return PBKDF2.randomChars(15);
        }

        return Iterables.getOnlyElement(pass).toCharArray();
    }
}
