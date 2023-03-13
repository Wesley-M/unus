package co.unus.controllers;

import co.unus.models.UnusUser;
import co.unus.services.UnusUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UnusUserController {
    private UnusUserService userService;

    UnusUserController(UnusUserService userService) {
        super();
        this.userService = userService;
    }

    @PostMapping(value = "/signup", consumes = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    void signup(@Valid @RequestBody UnusUser user) {
        userService.signup(user);
    }

    @GetMapping(value = "auth/ping")
    String status(Authentication authentication) {
        return authentication.getName();
    }
}
