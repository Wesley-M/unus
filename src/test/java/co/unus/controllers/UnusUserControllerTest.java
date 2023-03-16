package co.unus.controllers;

import co.unus.daos.UnusUserRepository;
import co.unus.models.UnusUser;
import co.unus.security.JwtRequest;
import co.unus.security.JwtResponse;
import co.unus.services.JwtService;
import co.unus.services.UnusUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureMockMvc
class UnusUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UnusUserService userService;

    @Autowired
    private UnusUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String USERS_ENDPOINT = "/api/users";

    private final String SIGNUP_ENDPOINT = USERS_ENDPOINT + "/signup";

    private final String LOGIN_ENDPOINT = USERS_ENDPOINT + "/authenticate";

    @AfterEach
    public void afterEachTest() {
        try {
            if(userRepository.count() > 0)
                userRepository.deleteAll();
        } catch (Exception ignored) {}
    }

    @BeforeEach
    public void beforeEachTest() {
        try {
            if(userRepository.count() > 0)
                userRepository.deleteAll();
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("Tests successful creation of user")
    public void endPointWhenSavingUser_shouldCreateUser() throws Exception {
        UnusUser user = new UnusUser("wesley@random.org", "123456", "wesley", LocalDate.parse("1999-11-29"));
        mockMvc.perform(post(SIGNUP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    @DisplayName("Tests creation of duplicate user")
    public void endPointWhenSavingDuplicateUser_shouldNotCreateUser() throws Exception {
        UnusUser user = new UnusUser("wesley@random.org", "123456", "wesley", LocalDate.parse("1999-11-29"));
        userService.signup(user);

        mockMvc.perform(post(SIGNUP_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Tests successful login")
    public void endPointWhenValidUserTriesLogin_shouldLogin() throws Exception {
        UnusUser user = new UnusUser("wesley@random.org", "123456", "wesley", LocalDate.parse("1999-11-29"));
        JwtRequest request = new JwtRequest(user.getEmail(), user.getPassword());
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));

        userService.signup(user);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @Test
    @Transactional
    @DisplayName("Tests user account deletion")
    public void endPointWhenUserTriesToDeleteAccount_shouldDeleteAccount() throws Exception {
        // Creating the user
        UnusUser user = new UnusUser("wesley@random.org", "123456", "wesley", LocalDate.parse("1999-11-29"));
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        userService.signup(user);

        JwtResponse login = jwtService.createToken(user.getEmail(), "123456");

        // Deleting the created user
        mockMvc.perform(delete(USERS_ENDPOINT)
                    .header("Authorization", "Bearer " + login.jwttoken()))
                .andExpect(status().is2xxSuccessful());

        Optional<UnusUser> storedUser = userRepository.findByEmail(user.getEmail());
        assertTrue(storedUser.isEmpty());
    }
}