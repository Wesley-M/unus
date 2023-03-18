package co.unus.services;

import co.unus.daos.GroupRepository;
import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.GroupInputDTO;
import co.unus.dtos.GroupOutputDTO;
import co.unus.models.Group;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupService {
    private final UnusUserService userService;

    private final UnusUserRepository userRepository;

    private final GroupRepository groupRepository;

    private final SpaceRepository spaceRepository;

    private final ModelMapper mapper = new ModelMapper();

    public GroupService(
            UnusUserService userService,
            UnusUserRepository userRepository,
            GroupRepository groupRepository,
            SpaceRepository spaceRepository
    ) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.spaceRepository = spaceRepository;
    }

    /**
     * Creates a new group
     *
     * Pre-conditions:
     *  1. In the same space, groups can't have the same name;
     *  2. The request's author should be a space member or admin;
     * */
    public GroupOutputDTO createGroup(GroupInputDTO dto, String email) {
        UnusUser user = getUser(email);
        Space space = getSpace(dto.getSpaceCode());

        throwIfGroupNameAlreadyExists(space, dto.getName());
        throwIfNotSpaceMemberOrAdmin(space, email);

        Group group = new Group(dto.getName(), dto.getIsOpen(), space, user);
        space.addGroup(group);
        spaceRepository.save(space);

        return mapper.map(groupRepository.save(group), GroupOutputDTO.class);
    }

    public void removeGroup(Long id, String email) {
        Group group = getGroup(id);
        throwIfNotAdmin(group, email, "Only admins can remove the group");
        group.getSpace().removeGroup(group);
        spaceRepository.save(group.getSpace());
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

    private void throwIfNotSpaceMemberOrAdmin(Space space, String email) {
        boolean isAdmin = space.getAdmin().getEmail().equals(email);
        boolean isMember = spaceRepository.isMember(space.getCode(), email);
        ExceptionThrower.throwIfIllegal(!isAdmin && !isMember, "User is not in the space");
    }

    private void throwIfGroupNameAlreadyExists(Space space, String groupName) {
        boolean alreadyExists = groupRepository.alreadyExistsInSpace(space.getCode(), groupName);
        ExceptionThrower.throwIfIllegal(alreadyExists, "Group name already exists in space");
    }

    private void throwIfNotAdmin(Group group, String email, String message) {
        boolean isNotGroupAdmin = !group.getAdmin().getEmail().equals(email);
        boolean isNotSpaceAdmin = !group.getSpace().getAdmin().getEmail().equals(email);
        ExceptionThrower.throwIfIllegal(isNotGroupAdmin && isNotSpaceAdmin, message);
    }

}
