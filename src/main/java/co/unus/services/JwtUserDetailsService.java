package co.unus.services;

import co.unus.models.UnusUser;
import co.unus.daos.UnusUserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    private UnusUserRepository userRepository;

    public JwtUserDetailsService(UnusUserRepository userRepository) {
        super();
        this.userRepository = userRepository;
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