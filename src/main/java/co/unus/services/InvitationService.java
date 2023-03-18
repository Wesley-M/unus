package co.unus.services;

import co.unus.daos.GroupRepository;
import co.unus.daos.InvitationRepository;
import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.InvitationInputDTO;
import co.unus.models.Group;
import co.unus.models.Invitation;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class InvitationService {

    private final UnusUserRepository userRepository;

    private final GroupRepository groupRepository;

    private final SpaceRepository spaceRepository;

    private final InvitationRepository invitationRepository;

    private final ModelMapper mapper = new ModelMapper();

    public InvitationService(
            UnusUserRepository userRepository,
            GroupRepository groupRepository,
            SpaceRepository spaceRepository,
            InvitationRepository invitationRepository
    ) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.spaceRepository = spaceRepository;
        this.invitationRepository = invitationRepository;
    }

    /**
     * Registers an invitation from a user without a group to the admin, and
     * vice-versa.
     *
     * Pre-conditions:
     * 1. The source is the one making the request;
     * 2. Source and target must be different;
     * 3. All these entities actually exist;
     * 4. All of them are in the same space;
     * 5. The source is the group admin, and it invites someone out who hasn't a group yet;
     * 6. The source isn't in a group, and it invites the group admin to accept it into the group;
     **/
    public Invitation invite(InvitationInputDTO dto, String email) {
        throwIfInvalidInvitation(dto, email);

        UnusUser source = getUser(dto.getSourceEmail(), "Source user was not found");
        UnusUser target = getUser(dto.getTargetEmail(), "Target user was not found");
        Group group = getGroup(dto.getGroupId());

        throwIfNotMember(group.getSpace(), source.getEmail(), "Source is not a member of the space");
        throwIfNotMember(group.getSpace(), target.getEmail(), "Target is not a member of the space");
        throwIfWrongSourceTargetCombination(source, target, group.getAdmin(), group.getSpace());

        Invitation invitation = new Invitation(source, target, group);
        return invitationRepository.save(invitation);
    }

    /**
     * Removes an invitation from the system.
     * Pre-conditions:
     * 1. The authenticated user must be the source or target of the invitation;
     * 2. The invitation actually exists;
     **/
    public Invitation removeInvitation(Long id, String email) {
        Invitation invitation = getInvitation(id);
        throwIfNotInvitationSourceOrTarget(invitation, email);

        invitation.getGroup().removeInvitation(invitation);
        groupRepository.save(invitation.getGroup());
        invitationRepository.deleteById(id);

        return invitation;
    }

    /**
     * Accepts an invitation.
     *
     * Pre-conditions:
     *  1. The target is the one making the request;
     *  2. The source or target can move around, so we have to validate if it still is a valid combination;
     * */
    @Transactional
    public void acceptInvitation(Long invitationId, String email) {
        Invitation invitation = getInvitation(invitationId);
        throwIfNotInvitationTarget(invitation, email);

        Group group = invitation.getGroup();

        throwIfWrongSourceTargetCombination(invitation.getSource(), invitation.getTarget(), group.getAdmin(), group.getSpace());
        invitationRepository.deleteById(invitationId);
        group.removeInvitation(invitation);

        boolean targetIsAdmin = invitation.getTarget().getEmail().equals(group.getAdmin().getEmail());
        group.addMember(targetIsAdmin ? invitation.getSource() : invitation.getTarget());
        groupRepository.save(group);
    }

    private UnusUser getUser(String email) {
        return getUser(email, "User was not found");
    }

    private UnusUser getUser(String email, String message) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(email);
        ExceptionThrower.throwIfNotFound(storedUser, message);
        return storedUser.get();
    }

    private Space getSpace(String code) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        ExceptionThrower.throwIfNotFound(storedSpace, "Space was not found");
        return storedSpace.get();
    }

    private Group getGroup(Long id) {
        Optional<Group> storedGroup = groupRepository.findById(id);
        ExceptionThrower.throwIfNotFound(storedGroup, "Group was not found");
        return storedGroup.get();
    }

    private Invitation getInvitation(Long id) {
        Optional<Invitation> storedInvitation = invitationRepository.findById(id);
        ExceptionThrower.throwIfNotFound(storedInvitation, "Invitation was not found");
        return storedInvitation.get();
    }

    private void throwIfNotMember(Space space, String email, String message) {
        boolean isNotMember = !spaceRepository.isMember(space.getCode(), email);
        ExceptionThrower.throwIfIllegal(isNotMember, message);
    }

    private void throwIfInvalidInvitation(InvitationInputDTO invitation, String email) {
        boolean isInvalidSource = !invitation.getSourceEmail().equals(email);
        boolean isInvalidSourceTargetCombination = invitation.getSourceEmail().equals(invitation.getTargetEmail());
        ExceptionThrower.throwIfIllegal(isInvalidSource, "The source must be the authenticated user");
        ExceptionThrower.throwIfIllegal(isInvalidSourceTargetCombination, "The source and target must be different");
    }

    private void throwIfWrongSourceTargetCombination(UnusUser source, UnusUser target, UnusUser groupAdmin, Space space) {
        boolean sourceIsAdmin = source.getEmail().equals(groupAdmin.getEmail());
        boolean targetIsAdmin = target.getEmail().equals(groupAdmin.getEmail());
        boolean sourceWithoutGroup = !groupRepository.hasGroupInSpace(space.getCode(), source.getEmail());
        boolean targetWithoutGroup = !groupRepository.hasGroupInSpace(space.getCode(), target.getEmail());
        boolean isValidCombination = (sourceIsAdmin && targetWithoutGroup) ^ (targetIsAdmin && sourceWithoutGroup);
        ExceptionThrower.throwIfIllegal(!isValidCombination, "Either the source is the group admin and the target is outside, or vice-versa");
    }

    private void throwIfNotInvitationTarget(Invitation invitation, String email) {
        boolean invalidTarget = !email.equals(invitation.getTarget().getEmail());
        ExceptionThrower.throwIfIllegal(invalidTarget, "User should be the target of invitation");
    }

    private void throwIfNotInvitationSourceOrTarget(Invitation invitation, String email) {
        boolean isInvitationSource = email.equals(invitation.getSource().getEmail());
        boolean isInvitationTarget = email.equals(invitation.getTarget().getEmail());
        ExceptionThrower.throwIfIllegal(!isInvitationSource && !isInvitationTarget, "User should be the source or target of invitation");
    }
}
