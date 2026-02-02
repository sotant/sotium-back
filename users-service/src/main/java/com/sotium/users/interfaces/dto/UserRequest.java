package com.sotium.users.interfaces.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotBlank @Email String email,
    @Size(min = 6, message = "Password must be at least 6 characters") String password,
    Boolean active
) {}
