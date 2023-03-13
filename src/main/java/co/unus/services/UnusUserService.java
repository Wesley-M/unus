package co.unus.services;

import co.unus.daos.UnusUserRepository;
import co.unus.exceptions.UserAlreadyExistsException;
import co.unus.models.UnusUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnusUserService {
    private final UnusUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(UnusUser user) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(user.getEmail());
        if (storedUser.isPresent()) {
            throw new UserAlreadyExistsException("Email already present in database: " + user.getEmail());
        }

        String password = passwordEncoder.encode(user.getPassword());
        user.setPassword(password);
        userRepository.save(user);
    }
}