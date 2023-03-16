package co.unus.controllers;

import co.unus.daos.GroupRepository;
import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.GroupInputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.dtos.SpaceOutputDTO;
import co.unus.models.Group;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.security.JwtResponse;
import co.unus.services.GroupService;
import co.unus.services.JwtService;
import co.unus.services.SpaceService;
import co.unus.services.UnusUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Transactional
class GroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpaceRepository spaceRepository;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private UnusUserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private GroupService groupService;

    @Autowired
    private UnusUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UnusUser spaceAdmin;

    private JwtResponse spaceAdminLogin;

    private final String GROUPS_ENDPOINT = "/api/auth/groups";

    private final ModelMapper mapper = new ModelMapper();

    private final String rawUserPassword = "123456";

    private UnusUser saveUser(String email, String name) throws Exception {
        UnusUser user = new UnusUser(
                email,
                passwordEncoder.encode(rawUserPassword),
                name,
                LocalDate.parse("1987-11-24")
        );
        userService.signup(user);
        return user;
    }

    private JwtResponse loginUser(String email) throws Exception {
        return jwtService.createToken(email, rawUserPassword);
    }

    @BeforeEach
    public void beforeTests() throws Exception {
        spaceAdmin = saveUser("admin@admin.com", "admin");
        spaceAdminLogin = loginUser(spaceAdmin.getEmail());
    }

    @AfterEach
    public void afterEachTests() {
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Tests successful creation of group")
    public void endPointWhenSavingGroup_shouldCreateGroup() throws Exception {
        Space space = new Space("random", "random", false, spaceAdmin);
        SpaceInputDTO spaceInput = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO storedSpace = spaceService.createSpace(spaceInput, spaceAdmin.getEmail());

        GroupInputDTO groupInput = new GroupInputDTO("random group", storedSpace.getCode(), false);

        mockMvc.perform(post(GROUPS_ENDPOINT)
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupInput)))
                .andExpect(status().is2xxSuccessful());

        Space currSpace = spaceRepository.findByCode(storedSpace.getCode()).get();
        assertEquals(1, currSpace.getGroups().size());
        assertEquals("random group", currSpace.getGroups().iterator().next().getName());
    }

    @Test
    @DisplayName("Tests creation of duplicate group")
    public void endPointWhenSavingDuplicateGroup_shouldNotCreateGroup() throws Exception {
        Space space = new Space("random", "random", false, spaceAdmin);
        SpaceInputDTO spaceInput = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO storedSpace = spaceService.createSpace(spaceInput, spaceAdmin.getEmail());

        GroupInputDTO groupInput = new GroupInputDTO("random group", storedSpace.getCode(), false);

        groupService.createGroup(groupInput, spaceAdmin.getEmail());
        mockMvc.perform(post(GROUPS_ENDPOINT)
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(groupInput)))
                .andExpect(status().is4xxClientError());

        // There is only the first group
        Space currSpace = spaceRepository.findByCode(storedSpace.getCode()).get();
        assertEquals(1, currSpace.getGroups().size());
        assertEquals("random group", currSpace.getGroups().iterator().next().getName());
    }
}