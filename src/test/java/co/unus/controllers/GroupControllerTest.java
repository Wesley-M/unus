package co.unus.controllers;

import co.unus.daos.GroupRepository;
import co.unus.daos.InvitationRepository;
import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.*;
import co.unus.models.Invitation;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.security.JwtResponse;
import co.unus.services.GroupService;
import co.unus.services.JwtService;
import co.unus.services.SpaceService;
import co.unus.services.UnusUserService;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
    private InvitationRepository invitationRepository;

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

    private final String INVITATIONS_ENDPOINT = "/api/auth/invitations";

    private final String GROUP_REMOVAL_ENDPOINT = GROUPS_ENDPOINT + "/%x";
    private final ModelMapper mapper = new ModelMapper();

    private final String rawUserPassword = "123456";

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
    @DisplayName("Tests successful creation of group")
    public void endPointWhenSavingGroup_shouldCreateGroup() throws Exception {
        SpaceOutputDTO storedSpace = createSpace();
        GroupInputDTO groupInput = new GroupInputDTO("random group", storedSpace.getCode(), false);

        authPostRequest(GROUPS_ENDPOINT, spaceAdminLogin.jwttoken(), groupInput)
                .andExpect(status().is2xxSuccessful());

        Space currSpace = spaceRepository.findByCode(storedSpace.getCode()).get();
        assertEquals(1, currSpace.getGroups().size());
        assertEquals("random group", currSpace.getGroups().iterator().next().getName());
    }

    @Test
    @DisplayName("Tests creation of duplicate group")
    public void endPointWhenSavingDuplicateGroup_shouldNotCreateGroup() throws Exception {
        SpaceOutputDTO storedSpace = createSpace();
        GroupInputDTO groupInput = new GroupInputDTO("random group", storedSpace.getCode(), false);

        groupService.createGroup(groupInput, spaceAdmin.getEmail());
        authPostRequest(GROUPS_ENDPOINT, spaceAdminLogin.jwttoken(), groupInput)
                .andExpect(status().is4xxClientError());

        Space currSpace = spaceRepository.findByCode(storedSpace.getCode()).get();
        assertEquals(1, currSpace.getGroups().size());
        assertEquals("random group", currSpace.getGroups().iterator().next().getName());
    }

    @Test
    @DisplayName("Tests creation of group as an outsider")
    public void endPointWhenTryingToCreateGroupAsOutsider_shouldNotCreateGroup() throws Exception {
        SpaceOutputDTO space = createSpace();
        JwtResponse outsiderLogin = addOutsider("outsider");

        GroupInputDTO groupInput = new GroupInputDTO("random group", space.getCode(), false);
        authPostRequest(GROUPS_ENDPOINT, outsiderLogin.jwttoken(), groupInput)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("User is not in the space", result.getResolvedException().getMessage()));

        Space currSpace = spaceRepository.findByCode(space.getCode()).get();
        assertEquals(0, currSpace.getGroups().size());
    }

    @Test
    @DisplayName("Tests successful removal of a group [Space Admin]")
    public void endPointWhenSpaceAdminRemovingGroup_shouldRemoveGroup() throws Exception {
        SpaceOutputDTO space = createSpace();
        addSpaceMember("member", space);

        GroupOutputDTO dto = createGroup("random group", space.getCode(), getStubEmail("member"));
        authDeleteRequest(GROUP_REMOVAL_ENDPOINT, spaceAdminLogin.jwttoken(), dto.getId())
                .andExpect(status().is2xxSuccessful());

        Space currSpace = spaceRepository.findByCode(space.getCode()).get();
        assertEquals(0, currSpace.getGroups().size());
    }

    @Test
    @DisplayName("Tests successful removal of a group [Group Admin]")
    public void endPointWhenGroupAdminRemovingGroup_shouldRemoveGroup() throws Exception {
        SpaceOutputDTO space = createSpace();
        JwtResponse memberLogin = addSpaceMember("member", space);
        GroupOutputDTO dto = createGroup("random group", space.getCode(), getStubEmail("member"));

        authDeleteRequest(GROUP_REMOVAL_ENDPOINT, memberLogin.jwttoken(), dto.getId())
                .andExpect(status().is2xxSuccessful());

        Space currSpace = spaceRepository.findByCode(space.getCode()).get();
        assertEquals(0, currSpace.getGroups().size());
    }

    @Test
    @DisplayName("Tests invalid removal of a group [OUTSIDER]")
    public void endPointWhenOutsiderTriesToRemoveGroup_shouldNotRemoveGroup() throws Exception {
        SpaceOutputDTO space = createSpace();
        JwtResponse outsiderLogin = addOutsider("outsider");

        GroupOutputDTO dto = createGroup("random group", space.getCode(), spaceAdmin.getEmail());
        authDeleteRequest(GROUP_REMOVAL_ENDPOINT, outsiderLogin.jwttoken(), dto.getId())
                .andExpect(status().is4xxClientError());

        Space currSpace = spaceRepository.findByCode(space.getCode()).get();
        assertEquals(1, currSpace.getGroups().size());
    }

    private ResultActions authPostRequest(String url, String token) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions authPostRequest(String url, String token, Object body) throws Exception {
        return mockMvc.perform(post(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private ResultActions authDeleteRequest(String url, String token, Long id) throws Exception {
        return mockMvc.perform(delete(String.format(url, id))
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private SpaceOutputDTO createSpace() {
        Space space = new Space("random", "random", false, spaceAdmin);
        SpaceInputDTO spaceInput = mapper.map(space, SpaceInputDTO.class);
        return spaceService.createSpace(spaceInput, spaceAdmin.getEmail());
    }

    private JwtResponse addSpaceMember(String memberId, SpaceOutputDTO space) throws Exception {
        UnusUser spaceMember = saveUser(getStubEmail(memberId), memberId);
        spaceService.joinSpaceAsMember(space.getCode(), spaceMember.getEmail());
        return loginUser(spaceMember.getEmail());
    }

    private JwtResponse addOutsider(String outsiderId) throws Exception {
        UnusUser outsider = saveUser(getStubEmail(outsiderId), outsiderId);
        return loginUser(outsider.getEmail());
    }

    private GroupOutputDTO createGroup(String name, String spaceCode, String authorEmail) {
        GroupInputDTO group = new GroupInputDTO(name, spaceCode, false);
        return groupService.createGroup(group, authorEmail);
    }

    private UnusUser saveUser(String email, String name) throws Exception {
        UnusUser user = new UnusUser(email, passwordEncoder.encode(rawUserPassword), name, LocalDate.parse("1987-11-24"));
        userService.signup(user);
        return user;
    }

    private JwtResponse loginUser(String email) throws Exception {
        return jwtService.createToken(email, rawUserPassword);
    }

    private String getStubEmail(String memberId) {
        return String.format("%s@%s.com", memberId, memberId);
    }
}