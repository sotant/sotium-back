package com.sotium.users.infrastructure.persistence;

import com.sotium.users.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getActive(),
                entity.getFechaCreacion(),
                entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }
        UserEntity entity = new UserEntity();
        entity.setId(domain.id());
        entity.setEmail(domain.email());
        entity.setPasswordHash(domain.passwordHash());
        entity.setActive(domain.active());
        entity.setFechaCreacion(domain.fechaCreacion());
        entity.setUpdatedAt(domain.updatedAt());
        return entity;
    }
}
