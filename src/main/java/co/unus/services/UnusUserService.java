package co.unus.services;

import co.unus.daos.UnusUserRepository;
import co.unus.exceptions.UserAlreadyExistsException;
import co.unus.models.UnusUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UnusUserService implements UserDetailsService  {
    private final UnusUserRepository userRepository;

    public UnusUserService(UnusUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void signup(UnusUser user) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(user.getEmail());
        if (storedUser.isPresent()) {
            throw new UserAlreadyExistsException("Email already present in database: " + user.getEmail());
        }

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UnusUser user = getUser(username);
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }

    public void removeAccount(String email) {
        // TODO: Manually remove related entities (e.g. Invitations, groups, spaces, ...)
        UnusUser user = getUser(email);
        userRepository.deleteByEmail(email);
    }

    private UnusUser getUser(String email) {
        return getUser(email, "User was not found");
    }

    private UnusUser getUser(String email, String message) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(email);
        ExceptionThrower.throwIfNotFound(storedUser, message);
        return storedUser.get();
    }
}