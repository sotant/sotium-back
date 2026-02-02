package com.sotium.users.interfaces.web;

import com.sotium.users.domain.model.User;
import com.sotium.users.interfaces.dto.UserRequest;
import com.sotium.users.interfaces.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserDtoMapper {

    public User toDomain(UserRequest request) {
        if (request == null) {
            return null;
        }
        return new User(
                null,
                request.email(),
                request.password(),
                request.active(),
                null,
                null
        );
    }

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.id(),
                user.email(),
                user.active(),
                user.fechaCreacion(),
                user.updatedAt()
        );
    }
}
