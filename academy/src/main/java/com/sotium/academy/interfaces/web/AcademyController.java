package com.sotium.academy.interfaces.web;

import com.sotium.academy.application.port.in.CreateAcademyUseCase;
import com.sotium.academy.domain.model.Academy;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/academies")
public class AcademyController {

    private final CreateAcademyUseCase createAcademyUseCase;

    public AcademyController(final CreateAcademyUseCase createAcademyUseCase) {
        this.createAcademyUseCase = createAcademyUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AcademyResponse create(@RequestBody final CreateAcademyRequest request) {
        final Academy academy = createAcademyUseCase.create(request.name());
        return new AcademyResponse(academy.id().value(), academy.name(), academy.active());
    }
}
