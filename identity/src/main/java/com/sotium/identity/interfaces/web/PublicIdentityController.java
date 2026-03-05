package com.sotium.identity.interfaces.web;

import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/identity")
@Slf4j
@RequiredArgsConstructor
public class PublicIdentityController {

    private final DeleteIdentityBySubUseCase deleteIdentityBySubUseCase;

    @GetMapping("/academy-registration")
    public ResponseEntity<Map<String, String>> registrationProbe() {
        log.debug("Public academy registration probe requested");
        return ResponseEntity.ok(Map.of("status", "registration endpoint available"));
    }

    @PostMapping("/purge-by-sub")
    public ResponseEntity<Map<String, Object>> purgeBySub(@RequestBody final DeleteIdentityBySubRequest request) {
        final var result = deleteIdentityBySubUseCase.delete(new DeleteIdentityBySubUseCase.DeleteIdentityBySubCommand(request.sub()));
        return ResponseEntity.ok(Map.of("deleted", result.deleted()));
    }

    public record DeleteIdentityBySubRequest(String sub) {
    }
}
