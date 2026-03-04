package com.sotium.identity.application.port.in;

public record EnsureIdentityUserCommand(
    String keycloakSub,
    String email
) {
}
