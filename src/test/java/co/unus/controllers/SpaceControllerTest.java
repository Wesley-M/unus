package co.unus.controllers;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.models.Space;
import co.unus.models.UnusUser;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureMockMvc
class SpaceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private UnusUserRepository userRepository;

    @Autowired
    private UnusUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UnusUser spaceAdmin;

    private JwtResponse spaceAdminLogin;

    private final String SPACES_ENDPOINT = "/api/auth/spaces";

    @BeforeEach
    public void beforeTests() throws Exception {
        spaceRepository.deleteAll();

        final String rawPassword = "123456";
        spaceAdmin = new UnusUser(
                "admin@admin.com",
                passwordEncoder.encode(rawPassword),
                "Administrator",
                LocalDate.parse("1987-11-24")
        );

        userService.signup(spaceAdmin);
        spaceAdminLogin = jwtService.createToken(spaceAdmin.getEmail(), rawPassword);
    }

    @AfterEach
    public void afterEachTests() {
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Tests successful creation of space")
    public void endPointWhenSavingSpace_shouldCreateSpace() throws Exception {
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        mockMvc.perform(post(SPACES_ENDPOINT)
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(space)))
                .andExpect(status().is2xxSuccessful());
    }
}