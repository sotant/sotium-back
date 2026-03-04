package com.sotium.academy.application.port.in;

public interface AcademyRegistrationPort {

    CreateAcademyResult createAcademy(CreateAcademyCommand command);
}
