package com.sotium.academy.application.port.out;

import com.sotium.academy.domain.model.Academy;

public interface AcademyRepositoryPort {

    Academy save(Academy academy);
}
