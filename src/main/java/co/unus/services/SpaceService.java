package co.unus.services;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.SpaceOutputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.dtos.UnusUserOutputDTO;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.utils.CodeGenerator;
import co.unus.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SpaceService {
    private final SpaceRepository spaceRepository;

    private final UnusUserRepository userRepository;

    private final ModelMapper mapper = new ModelMapper();

    public SpaceService(SpaceRepository spaceRepository, UnusUserRepository userRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SpaceOutputDTO createSpace(SpaceInputDTO dto, String email) {
        UnusUser user = getUser(email);

        Space space = new Space(dto.getName(), getUniqueCode(), dto.getIsPublic(), user);
        user.joinSpaceAsAdmin(space);
        userRepository.save(user);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public void removeSpace(String code, String email) {
        Space space = getSpace(code);
        throwIfNotSpaceAdmin(space, email, "Only the admin should remove the space");

        // TODO: Should consider a custom query. This is
        //  inefficient number-of-queries wise

        space.getMembers().forEach(m -> m.leaveSpace(space));
        userRepository.saveAll(space.getMembers());
        spaceRepository.deleteById(space.getId());
    }

    public SpaceOutputDTO getSpaceByCode(String code) {
        Space space = getSpace(code);
        return mapper.map(space, SpaceOutputDTO.class);
    }

    @Transactional
    public SpaceOutputDTO joinSpaceAsMember(String code, String email) {
        UnusUser user = getUser(email);
        Space space = getSpace(code);
        throwIfSpaceAdmin(space, email, "User is already the space admin.");

        user.joinSpace(space);
        userRepository.save(user);
        spaceRepository.save(space);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public void leaveSpaceAsMember(String code, String email) {
        UnusUser user = getUser(email);
        Space space = getSpace(code);
        throwIfSpaceAdmin(space, email, "The admin can not leave the space, delete it instead");
        throwIfNotSpaceMember(space, email, "Only a member of the space can leave it");

        user.leaveSpace(space);
        userRepository.save(user);
        spaceRepository.save(space);
    }

    public List<UnusUserOutputDTO> getMembers(String code, String email) {
        Space space = getSpace(code);
        throwIfNotSpaceMember(space, email, "Only a member can see other space members");

        List<UnusUser> members = space.getMembers().stream()
                .sorted()
                .collect(Collectors.toList());

        return ListMapper.mapList(members, UnusUserOutputDTO.class);
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
        ExceptionThrower.throwIfNotFound(storedSpace, "Space was not found.");
        return storedSpace.get();
    }

    private String getUniqueCode() {
        String code = "";
        do {
            code = CodeGenerator.get(8);
        } while(spaceRepository.findByCode(code).isPresent());
        return code;
    }

    private void throwIfSpaceAdmin(Space space, String email, String message) {
        boolean isSpaceAdmin = space.getAdmin().getEmail().equals(email);
        ExceptionThrower.throwIfIllegal(isSpaceAdmin, message);
    }

    private void throwIfNotSpaceAdmin(Space space, String email, String message) {
        boolean isSpaceAdmin = space.getAdmin().getEmail().equals(email);
        ExceptionThrower.throwIfIllegal(!isSpaceAdmin, message);
    }

    private void throwIfNotSpaceMember(Space space, String email, String message) {
        boolean isNotMember = !spaceRepository.isMember(space.getCode(), email);
        ExceptionThrower.throwIfIllegal(isNotMember, message);
    }
}
