package com.goeswhere.jiledrop.auth;

import com.goeswhere.jiledrop.types.Target;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.util.StringColumnMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/auth")
public class AuthResource {
    private static final Logger logger = LoggerFactory.getLogger(AuthResource.class);

    private final DBI dbi;

    public AuthResource(DBI dbi) {
        this.dbi = dbi;
    }

    private static final String BLIND_PASSWORD = PBKDF2.createHash("foo".toCharArray());

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public AuthResponse login(AuthRequest request) {
        logger.info("login request from '{}'", request.username);
        try (final Handle h = dbi.open()) {
            String pass = h.createQuery("select password from users where username=:username")
                    .bind("username", request.username)
                    .map(StringColumnMapper.INSTANCE)
                    .first();
            if (null == pass) {
                logger.info("login request from '{}': not found", request.username);
                pass = BLIND_PASSWORD;
            }

            if (!PBKDF2.validatePassword(request.password, pass)) {
                logger.info("login request from '{}': invalid password", request.username);
                return new AuthResponse();
            }

            final String target = h.createQuery("select target from users where username=:username")
                    .bind("username", request.username)
                    .map(StringColumnMapper.INSTANCE)
                    .first();

            logger.info("login request from '{}': success", request.username);
            return new AuthResponse(new Target(target));
        }
    }
}
