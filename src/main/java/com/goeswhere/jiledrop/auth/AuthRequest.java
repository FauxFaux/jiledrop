package com.goeswhere.jiledrop.auth;

import org.hibernate.validator.constraints.NotEmpty;

public class AuthRequest {
    @NotEmpty
    public String username;
    @NotEmpty
    public char[] password;
}
