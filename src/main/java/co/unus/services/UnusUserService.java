package co.unus.services;

import co.unus.daos.UnusUserRepository;
import co.unus.exceptions.UserAlreadyExistsException;
import co.unus.models.UnusUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnusUserService implements UserDetailsService  {
    private final UnusUserRepository userRepository;

    public void signup(UnusUser user) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(user.getEmail());
        if (storedUser.isPresent()) {
            throw new UserAlreadyExistsException("Email already present in database: " + user.getEmail());
        }

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UnusUser> tempUser = userRepository.findByEmail(username);

        if(tempUser.isPresent()) {
            return new User(tempUser.get().getEmail(), tempUser.get().getPassword(), new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User not found: " + username);
        }
    }
}