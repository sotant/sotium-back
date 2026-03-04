package com.sotium.academy.application.port.in;

public record CreateAcademyCommand(
    String name,
    String email,
    String phone,
    String timezone
) {
}
