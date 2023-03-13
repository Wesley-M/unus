package co.unus.controllers;

import co.unus.exceptions.SpaceNotFoundException;
import co.unus.exceptions.UserAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.http.HttpStatus;

@ControllerAdvice(basePackages = "co.unus.controllers")
public class ExceptionController {
    @ExceptionHandler(value = UserAlreadyExistsException.class)
    public ResponseEntity<String> handleUserAlreadyExistsException(UserAlreadyExistsException uae) {
        return new ResponseEntity<String>(uae.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(value = UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException unfe) {
        return new ResponseEntity<String>(unfe.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(value = SpaceNotFoundException.class)
    public ResponseEntity<String> handleSpaceNotFoundException(SpaceNotFoundException snfe) {
        return new ResponseEntity<String>(snfe.getMessage(), HttpStatus.NOT_FOUND);
    }
}
