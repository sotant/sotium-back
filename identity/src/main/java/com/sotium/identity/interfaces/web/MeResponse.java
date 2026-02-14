package com.sotium.identity.interfaces.web;

import java.util.Set;
import java.util.UUID;

public record MeResponse(
    String sub,
    String email,
    Set<String> authorities,
    UUID academyId
) {
}
