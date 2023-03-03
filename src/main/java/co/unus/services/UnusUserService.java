package co.unus.services;

import co.unus.daos.UnusUserRepository;
import co.unus.exceptions.UserAlreadyExistsException;
import co.unus.models.UnusUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UnusUserService {
    private UnusUserRepository<UnusUser, String> userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public UnusUserService(UnusUserRepository<UnusUser, String> userRepository) {
        super();
        this.userRepository = userRepository;
    }

    public void signup(UnusUser user) {
        Optional<UnusUser> storedUser = userRepository.findById(user.getEmail());
        if(!storedUser.isPresent()) {
            String password = passwordEncoder.encode(user.getPassword());
            user.setPassword(password);
            userRepository.save(user);
        } else {
            throw new UserAlreadyExistsException("Email already present in database: " + user.getEmail());
        }
    }
}