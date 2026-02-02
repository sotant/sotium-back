package com.sotium.users.application.port.out;

public interface PasswordEncoder {
    String encode(String password);
}
