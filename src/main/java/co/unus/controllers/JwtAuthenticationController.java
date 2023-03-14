package co.unus.controllers;

import co.unus.security.JwtRequest;
import co.unus.security.JwtResponse;
import co.unus.services.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/api/users")
public class JwtAuthenticationController {

    @Autowired
    private JwtService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/authenticate", consumes = "application/json")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        String username = authenticationRequest.username();
        String password = authenticationRequest.password();
        JwtResponse response = userService.createToken(username, password);
        return ResponseEntity.ok(response);
    }
}
