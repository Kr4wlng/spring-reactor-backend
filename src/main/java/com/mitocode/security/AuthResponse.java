package com.mitocode.security;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthResponse(@JsonProperty(value = "access_token") String token) {
}
