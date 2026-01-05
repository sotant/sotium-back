package com.sotium.users.web;

import com.sotium.users.dto.UserResponse;
import com.sotium.users.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerTest {

    @LocalServerPort
    int port;

    RestClient rest;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void clean() {
        userRepository.deleteAll();
        rest = RestClient.builder().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @DisplayName("POST /api/users crea un usuario y no expone password_hash")
    void createUser_success() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "user1@example.com");
        body.put("password", "secreto1");
        body.put("active", true);

        ResponseEntity<UserResponse> res = rest.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(UserResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UserResponse u = res.getBody();
        assertThat(u).isNotNull();
        assertThat(u.getId()).isNotNull();
        assertThat(u.getEmail()).isEqualTo("user1@example.com");
        assertThat(u.getActive()).isTrue();
        assertThat(u.getFechaCreacion()).isNotNull();
        assertThat(u.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("POST /api/users con email duplicado devuelve 409")
    void createUser_duplicateEmail_conflict() {
        create("dup@example.com", "secreto1", true);
        try {
            rest.post()
                    .uri("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("email", "dup@example.com", "password", "secreto2", "active", true))
                    .retrieve()
                    .toEntity(Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }
    }

    @Test
    @DisplayName("GET /api/users lista los usuarios")
    void listUsers() {
        create("a1@example.com", "secreto1", true);
        create("a2@example.com", "secreto2", false);

        ResponseEntity<List<UserResponse>> res = rest.get()
                .uri("/api/users")
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<UserResponse>>() {});
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<UserResponse> list = res.getBody();
        assertThat(list).isNotNull();
        assertThat(list).hasSize(2);
        assertThat(list.get(0).getEmail()).isEqualTo("a1@example.com");
        assertThat(list.get(1).getEmail()).isEqualTo("a2@example.com");
    }

    @Test
    @DisplayName("GET /api/users/{id} 404 cuando no existe")
    void get_notFound() {
        try {
            rest.get()
                    .uri("/api/users/{id}", 9999)
                    .retrieve()
                    .toEntity(Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("PUT /api/users/{id} actualiza campos")
    void update_success() {
        long id = create("to.update@example.com", "secreto1", true);
        Map<String, Object> body = new HashMap<>();
        body.put("active", false);
        body.put("password", "nuevoPass");
        body.put("email", "to.update@example.com");

        ResponseEntity<UserResponse> res = rest.put()
                .uri("/api/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(UserResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserResponse u = res.getBody();
        assertThat(u).isNotNull();
        assertThat(u.getId()).isEqualTo(id);
        assertThat(u.getEmail()).isEqualTo("to.update@example.com");
        assertThat(u.getActive()).isFalse();
    }

    @Test
    @DisplayName("DELETE /api/users/{id} elimina y luego GET devuelve 404")
    void delete_success() {
        long id = create("to.delete@example.com", "secreto1", true);
        rest.delete().uri("/api/users/{id}", id).retrieve();
        try {
            rest.get()
                    .uri("/api/users/{id}", id)
                    .retrieve()
                    .toEntity(Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    @DisplayName("Validación: email inválido y password muy corto → 400")
    void validation_errors_badRequest() {
        Map<String, Object> body = new HashMap<>();
        body.put("email", "no-es-email");
        body.put("password", "123");
        body.put("active", true);

        try {
            rest.post()
                    .uri("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toEntity(Map.class);
        } catch (HttpClientErrorException e) {
            assertThat(e.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    // helper to create user and return id
    private long create(String email, String password, boolean active) {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        body.put("active", active);
        ResponseEntity<UserResponse> res = rest.post()
                .uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toEntity(UserResponse.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        return res.getBody().getId();
    }
}
