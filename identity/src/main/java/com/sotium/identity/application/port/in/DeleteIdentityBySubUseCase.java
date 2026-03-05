package com.sotium.identity.application.port.in;

public interface DeleteIdentityBySubUseCase {

    DeleteIdentityBySubResult delete(DeleteIdentityBySubCommand command);

    record DeleteIdentityBySubCommand(String sub) {
    }

    record DeleteIdentityBySubResult(boolean deleted) {
    }
}
