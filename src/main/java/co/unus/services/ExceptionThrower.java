package co.unus.services;

import co.unus.exceptions.ResourceAlreadyExistsException;
import co.unus.exceptions.ResourceNotFoundException;

import java.util.Optional;

public class ExceptionThrower {
    public static void throwIfNotFound(Optional<?> obj, String message) {
        if (obj.isEmpty()) {
            throw new ResourceNotFoundException(message);
        }
    }

    public static void throwIfAlreadyExists(boolean alreadyExists, String message) {
        if (alreadyExists) {
            throw new ResourceAlreadyExistsException(message);
        }
    }

    public static void throwIfIllegal(boolean isIllegal, String message) {
        if (isIllegal) {
            throw new IllegalArgumentException(message);
        }
    }
}
