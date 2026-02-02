package com.sotium.users.application.port.in;

import com.sotium.users.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserUseCase {
    User createUser(User user);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
    User updateUser(User user);
    void deleteUser(Long id);
}
