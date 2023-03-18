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
class InvitationControllerTest {
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

    private final String INVITATIONS_ENDPOINT = "/api/auth/invitations";

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
    @DisplayName("Tests creation of invitation")
    public void endPointWhenCreatingInvitation_shouldCreateInvitation() throws Exception {
        SpaceOutputDTO space = createSpace();
        JwtResponse member1Login = addSpaceMember("member1", space);
        addSpaceMember("member2", space);
        GroupOutputDTO group = createGroup("random group", space.getCode(), getStubEmail("member1"));

        InvitationInputDTO input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("member2"), group.getId());
        ResultActions resultActions = authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is2xxSuccessful());

        invitationResponseExists(resultActions);
    }

    @Test
    @DisplayName("Tests creation of invitation [INVALID]")
    public void endPointWhenCreatingInvalidInvitation_shouldNotCreateInvitation() throws Exception {
        SpaceOutputDTO space = createSpace();
        JwtResponse member1Login = addSpaceMember("member1", space);
        JwtResponse member2Login = addSpaceMember("member2", space);
        JwtResponse member3Login = addSpaceMember("member3", space);
        JwtResponse member4Login = addSpaceMember("member4", space);
        JwtResponse member5Login = addSpaceMember("member5", space);
        JwtResponse outsider1Login = addOutsider("outsider1");
        JwtResponse outsider2Login = addOutsider("outsider2");

        GroupOutputDTO group1 = createGroup("random group", space.getCode(), getStubEmail("member1"));
        GroupOutputDTO group2 = createGroup("random group 2", space.getCode(), getStubEmail("member5"));

        // Same source and target users
        InvitationInputDTO input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("member1"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("The source and target must be different", result.getResolvedException().getMessage()));

        // Source user is different from the one making the invitation
        input = new InvitationInputDTO(getStubEmail("member2"), getStubEmail("member1"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("The source must be the authenticated user", result.getResolvedException().getMessage()));

        // Target user not found
        input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("notfound"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Target user was not found", result.getResolvedException().getMessage()));

        // Group was not found
        input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("member2"), group1.getId() + 100);
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Group was not found", result.getResolvedException().getMessage()));

        // There is no group admin here
        input = new InvitationInputDTO(getStubEmail("member3"), getStubEmail("member4"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member3Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Either the source is the group admin and the target is outside, or vice-versa", result.getResolvedException().getMessage()));

        // The two users are group admins
        input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("member5"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Either the source is the group admin and the target is outside, or vice-versa", result.getResolvedException().getMessage()));

        // Trying to invite an outsider
        input = new InvitationInputDTO(getStubEmail("member1"), getStubEmail("outsider1"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, member1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Target is not a member of the space", result.getResolvedException().getMessage()));

        // Outsider trying to invite group admin
        input = new InvitationInputDTO(getStubEmail("outsider1"), getStubEmail("member1"), group1.getId());
        authPostRequest(INVITATIONS_ENDPOINT, outsider1Login.jwttoken(), input)
                .andExpect(status().is4xxClientError())
                .andExpect(result -> assertEquals("Source is not a member of the space", result.getResolvedException().getMessage()));
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

    private void invitationResponseExists(ResultActions resultActions) throws UnsupportedEncodingException, JsonProcessingException {
        MvcResult result = resultActions.andReturn();
        Invitation returnedInvitation = objectMapper.readValue(result.getResponse().getContentAsString(), Invitation.class);
        Optional<Invitation> storedInvitation = invitationRepository.findById(returnedInvitation.getId());
        assertTrue(storedInvitation.isPresent());
    }

    private String getStubEmail(String memberId) {
        return String.format("%s@%s.com", memberId, memberId);
    }
}