package co.unus.services;

import co.unus.daos.GroupRepository;
import co.unus.daos.InvitationRepository;
import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.InvitationInputDTO;
import co.unus.dtos.GroupInputDTO;
import co.unus.dtos.GroupOutputDTO;
import co.unus.exceptions.*;
import co.unus.models.Group;
import co.unus.models.Invitation;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class GroupService {
    private final UnusUserService userService;

    private final UnusUserRepository userRepository;

    private final GroupRepository groupRepository;

    private final SpaceRepository spaceRepository;

    private final InvitationRepository invitationRepository;

    private final ModelMapper mapper = new ModelMapper();

    public GroupService(
            UnusUserService userService,
            UnusUserRepository userRepository,
            GroupRepository groupRepository,
            SpaceRepository spaceRepository,
            InvitationRepository invitationRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.spaceRepository = spaceRepository;
        this.invitationRepository = invitationRepository;
    }

    public GroupOutputDTO createGroup(GroupInputDTO dto, String adminEmail) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(adminEmail);
        UnusUser user = storedUser.get();

        Space space = getSpace(dto.getSpaceCode());
        Group group = new Group(dto.getName(), dto.getIsOpen(), space, user);
        throwIfGroupNameAlreadyExists(space, group.getName());
        space.addGroup(group);

        spaceRepository.save(space);

        return mapper.map(group, GroupOutputDTO.class);
    }

    public void removeGroup(Long id, String email) {
        Group group = getGroup(id);
        throwIfNotAdmin(group, email, "Only the admin should remove the group");
        groupRepository.deleteById(id);
    }

    public void invite(InvitationInputDTO dto, String email) {
        // dto.sourceEmail has to match email

        Optional<UnusUser> storedSourceUser = userRepository.findByEmail(dto.getSourceEmail());
        throwIfUserNotFound(storedSourceUser, "Email from the source user was not used");
        Optional<UnusUser> storedTargetUser = userRepository.findByEmail(dto.getTargetEmail());
        throwIfUserNotFound(storedTargetUser, "Email from the target user was not used");
        Optional<Group> storedGroup = groupRepository.findById(dto.getGroupId());
        throwIfGroupNotFound(storedGroup);

        UnusUser source = storedSourceUser.get();
        UnusUser target = storedTargetUser.get();
        Group group = storedGroup.get();
        Boolean sentByAdmin = source.getEmail().equals(group.getAdmin().getEmail());

        Invitation invitation = new Invitation(source, target, group, sentByAdmin);

        invitationRepository.save(invitation);
    }

    public void removeInvitation(Long id, String email) {
        Invitation invitation = getInvitation(id);
        throwIfNotInvitationSourceOrTarget(invitation, email);

        invitation.getGroup().removeInvitation(invitation);
        groupRepository.save(invitation.getGroup());
        invitationRepository.deleteById(id);
    }

    @Transactional
    public void acceptInvitation(Long id, String email) {
        Invitation invitation = getInvitation(id);
        throwIfNotInvitationTarget(invitation, email);

        UnusUser user = userRepository.findByEmail(email).get();
        invitationRepository.deleteById(id);

        Group group = invitation.getGroup();
        group.removeInvitation(invitation);
        group.addMember(user);
        groupRepository.save(group);
    }

    private Space getSpace(String code) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        throwIfSpaceNotFound(storedSpace);
        return storedSpace.get();
    }

    private Group getGroup(Long id) {
        Optional<Group> storedGroup = groupRepository.findById(id);
        throwIfGroupNotFound(storedGroup);
        return storedGroup.get();
    }

    private Invitation getInvitation(Long id) {
        Optional<Invitation> storedInvitation = invitationRepository.findById(id);
        throwIfInvitationNotFound(storedInvitation);
        return storedInvitation.get();
    }

    private void throwIfGroupNotFound(Optional<Group> group) {
        if (group.isEmpty()) {
            throw new ResourceNotFoundException("Group was not found.");
        }
    }

    private void throwIfGroupNameAlreadyExists(Space space, String groupName) {
        if (groupRepository.alreadyExistsInSpace(space.getCode(), groupName)) {
            throw new ResourceAlreadyExistsException("Group name already exists in space.");
        }
    }

    private void throwIfSpaceNotFound(Optional<Space> space) {
        if (space.isEmpty()) {
            throw new ResourceNotFoundException("Space was not found.");
        }
    }

    private void throwIfNotAdmin(Group group, String email, String message) {
        if (!group.getAdmin().getEmail().equals(email)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void throwIfUserNotFound(Optional<UnusUser> storedUser, String message) {
        if (storedUser.isEmpty()) {
            throw new UsernameNotFoundException(message);
        }
    }

    private void throwIfInvitationNotFound(Optional<Invitation> storedInvitation) {
        if (storedInvitation.isEmpty()) {
            throw new ResourceNotFoundException("Invitation was not found");
        }
    }

    private void throwIfNotInvitationTarget(Invitation invitation, String email) {
        if (!email.equals(invitation.getTarget().getEmail())) {
            throw new IllegalArgumentException("User should be the target of invitation");
        }
    }

    private void throwIfNotInvitationSourceOrTarget(Invitation invitation, String email) {
        if (!email.equals(invitation.getSource().getEmail()) && !email.equals(invitation.getTarget().getEmail())) {
            throw new IllegalArgumentException("User should be the source or target of invitation");
        }
    }

}
