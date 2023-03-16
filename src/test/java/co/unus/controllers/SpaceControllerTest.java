package co.unus.controllers;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.SpaceInputDTO;
import co.unus.dtos.SpaceOutputDTO;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.security.JwtResponse;
import co.unus.services.JwtService;
import co.unus.services.SpaceService;
import co.unus.services.UnusUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@EnableAutoConfiguration
@AutoConfigureMockMvc
@Transactional
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
    private SpaceService spaceService;

    @Autowired
    private UnusUserRepository userRepository;

    @Autowired
    private UnusUserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UnusUser spaceAdmin;

    private JwtResponse spaceAdminLogin;

    private final String SPACES_ENDPOINT = "/api/auth/spaces";

    private final String SPACE_ENDPOINT = SPACES_ENDPOINT + "/%s";

    private final String JOIN_SPACE_ENDPOINT = SPACES_ENDPOINT + "/%s/join";

    private final String LEAVE_SPACE_ENDPOINT = SPACES_ENDPOINT + "/%s/leave";

    private final String GET_MEMBERS_ENDPOINT = SPACES_ENDPOINT + "/%s/members";

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
        spaceRepository.deleteAll();
        spaceAdmin = saveUser("admin@admin.com", "admin");
        spaceAdminLogin = loginUser(spaceAdmin.getEmail());
    }

    @AfterEach
    public void afterEachTests() {
        spaceRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Tests protection of endpoints")
    public void endPointWhenTryingToAccessProtectedEndpoint_shouldLetItHappen() throws Exception {
        mockMvc.perform(post(SPACES_ENDPOINT).content("")).andExpect(status().is(401));
        mockMvc.perform(post(String.format(SPACE_ENDPOINT, "random"))).andExpect(status().is(401));
        mockMvc.perform(post(String.format(JOIN_SPACE_ENDPOINT, "random"))).andExpect(status().is(401));
    }

    @Test
    @DisplayName("Tests successful creation of space")
    public void endPointWhenSavingSpace_shouldCreateSpace() throws Exception {
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);

        mockMvc.perform(post(SPACES_ENDPOINT)
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().is2xxSuccessful());

        UnusUser adm = userRepository.findByEmail(spaceAdmin.getEmail()).get();
        assertEquals(adm.getAdministeredSpaces().size(), 1);
    }

    @Test
    @DisplayName("Tests getting space by code")
    public void endPointWhenPassingCode_shouldGetSpace() throws Exception {
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);

        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        mockMvc.perform(get(String.format(SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(output)));
    }

    @Test
    @DisplayName("Tests trying to remove empty space by code")
    public void endPointWhenTryingToRemoveEmptySpaceByCode_shouldRemove() throws Exception {
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);

        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        mockMvc.perform(delete(String.format(SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful());

        Optional<Space> storedSpace = spaceRepository.findByCode(space.getCode());
        assertTrue(storedSpace.isEmpty());
    }

    @Test
    @DisplayName("Tests trying to remove space with members by code")
    public void endPointWhenTryingToRemoveSpacesWithMembersByCode_shouldRemove() throws Exception {
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceMember = saveUser("member@member.com", "member");

        // Trying to join
        spaceService.joinSpaceAsMember(output.getCode(), spaceMember.getEmail());

        mockMvc.perform(delete(String.format(SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful());

        Optional<Space> storedSpace = spaceRepository.findByCode(space.getCode());
        UnusUser storedUser = userRepository.findById(spaceMember.getId()).get();
        assertTrue(storedSpace.isEmpty());
        assertEquals(storedUser.getJoinedSpaces().size(), 0);
    }

    @Test
    @DisplayName("Tests join public space")
    public void endPointWhenTryingToJoinPublicSpace_shouldJoinSpace() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceMember = saveUser("member@member.com", "member");
        JwtResponse spaceMemberLogin = loginUser(spaceMember.getEmail());

        // Trying to join
        mockMvc.perform(post(String.format(JOIN_SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceMemberLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(output)));

        // Member was added
        Space storedSpace = spaceRepository.findByCode(output.getCode()).get();
        UnusUser storedUser = userRepository.findById(spaceMember.getId()).get();
        assertEquals(storedSpace.getMembers().size(), 1);
        assertEquals(storedUser.getJoinedSpaces().size(), 1);
    }

    @Test
    @DisplayName("Tries to leave space as a member")
    public void endPointWhenTryingLeaveSpaceAsMember_shouldLeaveSpace() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceMember = saveUser("member@member.com", "member");
        JwtResponse spaceMemberLogin = loginUser(spaceMember.getEmail());

        // Trying to join
        spaceService.joinSpaceAsMember(output.getCode(), spaceMember.getEmail());

        mockMvc.perform(delete(String.format(LEAVE_SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceMemberLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful());

        Boolean isMember = spaceRepository.isMember(output.getCode(), spaceMember.getEmail());
        assertFalse(isMember);
    }

    @Test
    @DisplayName("Tries to leave space as an admin/outsider")
    public void endPointWhenTryingInvalidLeaveSpace_shouldNotLeaveSpace() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceMember = saveUser("member@member.com", "member");
        JwtResponse spaceMemberLogin = loginUser(spaceMember.getEmail());

        mockMvc.perform(delete(String.format(LEAVE_SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceMemberLogin.jwttoken()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Only a member of the space can leave it", result.getResolvedException().getMessage()));

        mockMvc.perform(delete(String.format(LEAVE_SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("The admin can not leave the space, delete it instead", result.getResolvedException().getMessage()));
    }

    @Test
    @DisplayName("Tests trying to join a space where the user is already the admin")
    public void endPointWhenTryingToJoinSpaceAsAdmin_shouldNotLetJoin() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Trying to join
        mockMvc.perform(post(String.format(JOIN_SPACE_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceAdminLogin.jwttoken()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("User is already the space admin.", result.getResolvedException().getMessage()));
    }

    @Test
    @DisplayName("Trying to access the members of a space")
    public void endPointWhenGetMembers_shouldReturnMembers() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceMember = saveUser("member@member.com", "member");
        JwtResponse spaceMemberLogin = loginUser(spaceMember.getEmail());

        // Trying to join
        spaceService.joinSpaceAsMember(output.getCode(), spaceMember.getEmail());

        mockMvc.perform(get(String.format(GET_MEMBERS_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceMemberLogin.jwttoken()))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$[0].name", is(spaceMember.getName())));
    }

    @Test
    @DisplayName("Trying to access the members of a space as an outsider")
    public void endPointWhenGetMembersAsAnOutsider_shouldNotReturnMembers() throws Exception {
        // Creating the space
        Space space = new Space("random", "werwe34", false, spaceAdmin);
        SpaceInputDTO input = mapper.map(space, SpaceInputDTO.class);
        SpaceOutputDTO output = spaceService.createSpace(input, spaceAdmin.getEmail());

        // Creating the member
        UnusUser spaceOutsider = saveUser("outsider@outsider.com", "outsider");
        JwtResponse spaceOutsiderLogin = loginUser(spaceOutsider.getEmail());

        mockMvc.perform(get(String.format(GET_MEMBERS_ENDPOINT, output.getCode()))
                        .header("Authorization", "Bearer " + spaceOutsiderLogin.jwttoken()))
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Only a member can see other space members", result.getResolvedException().getMessage()));
    }
}