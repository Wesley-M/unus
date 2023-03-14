package co.unus.controllers;

import co.unus.models.UnusUser;
import co.unus.services.UnusUserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UnusUserController {
    @Autowired
    private UnusUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping(value = "/signup", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    void signup(@Valid @RequestBody UnusUser user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        userService.signup(user);
    }
}
