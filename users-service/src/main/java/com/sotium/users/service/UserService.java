package com.sotium.users.service;

import com.sotium.users.dto.UserRequest;
import com.sotium.users.dto.UserResponse;
import com.sotium.users.model.User;
import com.sotium.users.repo.UserRepository;
import com.sotium.users.web.ConflictException;
import com.sotium.users.web.NotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public UserResponse create(UserRequest req) {
        if (repository.existsByEmailIgnoreCase(req.getEmail())) {
            throw new ConflictException("Email already exists: " + req.getEmail());
        }
        User u = new User();
        u.setEmail(req.getEmail());
        if (req.getPassword() == null || req.getPassword().isBlank()) {
            throw new ConflictException("Password is required");
        }
        u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        if (req.getActive() != null) {
            u.setActive(req.getActive());
        }
        User saved = repository.save(u);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return repository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id) {
        User u = repository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));
        return toResponse(u);
    }

    public UserResponse update(Long id, UserRequest req) {
        User u = repository.findById(id).orElseThrow(() -> new NotFoundException("User not found: " + id));

        if (req.getEmail() != null && !req.getEmail().equalsIgnoreCase(u.getEmail())) {
            if (repository.existsByEmailIgnoreCase(req.getEmail())) {
                throw new ConflictException("Email already exists: " + req.getEmail());
            }
            u.setEmail(req.getEmail());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            u.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getActive() != null) {
            u.setActive(req.getActive());
        }
        User saved = repository.save(u);
        return toResponse(saved);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("User not found: " + id);
        }
        repository.deleteById(id);
    }

    private UserResponse toResponse(User u) {
        UserResponse resp = new UserResponse();
        resp.setId(u.getId());
        resp.setEmail(u.getEmail());
        resp.setActive(u.getActive());
        resp.setFechaCreacion(u.getFechaCreacion());
        resp.setUpdatedAt(u.getUpdatedAt());
        return resp;
    }
}
