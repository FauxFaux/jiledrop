package com.goeswhere.jiledrop.auth;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.goeswhere.jiledrop.types.Target;

public class AuthResponse {
    public Target target;

    public AuthResponse() {
    }

    @JsonCreator
    public AuthResponse(@JsonProperty("target") Target target) {
        this.target = target;
    }
}
