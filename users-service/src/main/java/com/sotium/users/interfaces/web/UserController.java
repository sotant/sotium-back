package com.sotium.users.interfaces.web;

import com.sotium.shared.exception.NotFoundException;
import com.sotium.users.application.port.in.UserUseCase;
import com.sotium.users.domain.model.User;
import com.sotium.users.interfaces.dto.UserRequest;
import com.sotium.users.interfaces.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserDtoMapper userDtoMapper;

    public UserController(UserUseCase userUseCase, UserDtoMapper userDtoMapper) {
        this.userUseCase = userUseCase;
        this.userDtoMapper = userDtoMapper;
    }

    @GetMapping
    public List<UserResponse> list() {
        return userUseCase.getAllUsers().stream()
                .map(userDtoMapper::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return userUseCase.getUserById(id)
                .map(userDtoMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("User not found: " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserRequest request) {
        User user = userDtoMapper.toDomain(request);
        User createdUser = userUseCase.createUser(user);
        return userDtoMapper.toResponse(createdUser);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        User user = userDtoMapper.toDomain(request);
        // TODO: This is not correct, we need to merge the existing user with the request
        User updatedUser = userUseCase.updateUser(user);
        return userDtoMapper.toResponse(updatedUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userUseCase.deleteUser(id);
    }
}
