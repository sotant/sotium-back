package com.sotium.academy.application.port.in;

import com.sotium.academy.domain.model.Academy;

public interface CreateAcademyUseCase {

    Academy create(String name);
}
