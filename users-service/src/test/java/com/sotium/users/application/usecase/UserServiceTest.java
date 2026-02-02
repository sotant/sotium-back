package com.sotium.users.application.usecase;

import com.sotium.users.application.port.output.PasswordEncoder;
import com.sotium.users.domain.model.User;
import com.sotium.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User(
                1L,
                "test@example.com",
                "rawPassword",
                true,
                LocalDate.now(),
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("createUser should hash password and save user")
    void createUser_ShouldHashPasswordAndSave() {
        // Given
        when(userRepository.existsByEmail(sampleUser.email())).thenReturn(false);
        when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            return new User(1L, u.email(), u.passwordHash(), u.active(), u.fechaCreacion(), u.updatedAt());
        });

        // When
        User createdUser = userService.createUser(sampleUser);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.passwordHash()).isEqualTo("hashedPassword");
        verify(passwordEncoder).encode("rawPassword");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("getUserById should return user when found")
    void getUserById_ShouldReturnUser() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().email()).isEqualTo(sampleUser.email());
    }

    @Test
    @DisplayName("getUserById should return empty when not found")
    void getUserById_ShouldReturnEmpty() {
        // Given
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(99L);

        // Then
        assertThat(result).isEmpty();
    }
}
