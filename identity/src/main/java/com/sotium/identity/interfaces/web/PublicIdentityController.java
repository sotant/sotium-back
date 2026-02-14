package com.sotium.identity.interfaces.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/public/identity")
@Slf4j
public class PublicIdentityController {

    @GetMapping("/academy-registration")
    public ResponseEntity<Map<String, String>> registrationProbe() {
        log.debug("Public academy registration probe requested");
        return ResponseEntity.ok(Map.of("status", "registration endpoint available"));
    }
}
