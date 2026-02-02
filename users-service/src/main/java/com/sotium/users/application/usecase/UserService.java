package com.sotium.users.application.usecase;

import com.sotium.users.application.port.in.UserUseCase;
import com.sotium.users.application.port.output.PasswordEncoder;
import com.sotium.users.domain.model.User;
import com.sotium.users.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.email())) {
            // TODO: Throw custom exception
        }
        String hashedPassword = passwordEncoder.encode(user.passwordHash());
        User userToSave = new User(
                null,
                user.email(),
                hashedPassword,
                user.active(),
                user.fechaCreacion(),
                user.updatedAt()
        );
        return userRepository.save(userToSave);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        // TODO: Implement pagination
        return userRepository.findAll();
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
