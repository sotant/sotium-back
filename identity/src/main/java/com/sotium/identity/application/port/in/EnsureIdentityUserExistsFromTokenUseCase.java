package com.sotium.identity.application.port.in;

public interface EnsureIdentityUserExistsFromTokenUseCase {

    EnsureIdentityUserResult ensure(EnsureIdentityUserCommand command);
}
