package co.unus.services;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.SpaceOutputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.dtos.UnusUserOutputDTO;
import co.unus.exceptions.ResourceNotFoundException;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.utils.CodeGenerator;
import co.unus.utils.ListMapper;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

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

    public SpaceOutputDTO createSpace(SpaceInputDTO dto, String adminEmail) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(adminEmail);
        UnusUser user = storedUser.get();

        Space space = new Space(dto.getName(), getUniqueCode(), dto.getIsPublic(), user);
        user.joinSpaceAsAdmin(space);
        userRepository.save(user);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public void removeSpace(String code, String email) {
        Space space = getSpace(code);
        throwIfNotAdmin(space, email, "Only the admin should remove the space");

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

    public SpaceOutputDTO joinSpaceAsMember(String code, String email) {
        Space space = getSpace(code);
        throwIfAdmin(space, email, "User is already the space admin.");

        Optional<UnusUser> storedUser = userRepository.findByEmail(email);
        UnusUser user = storedUser.get();
        user.joinSpace(space);

        userRepository.save(user);
        spaceRepository.save(space);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public void leaveSpaceAsMember(String code, String email) {
        Space space = getSpace(code);
        throwIfAdmin(space, email, "The admin can not leave the space, delete it instead");
        throwIfNotMember(space, email, "Only a member of the space can leave it");

        Optional<UnusUser> storedUser = userRepository.findByEmail(email);
        UnusUser user = storedUser.get();
        user.leaveSpace(space);

        userRepository.save(user);
        spaceRepository.save(space);
    }

    public List<UnusUserOutputDTO> getMembers(String code, String email) {
        Space space = getSpace(code);
        throwIfNotMember(space, email, "Only a member can see other space members");

        List<UnusUser> members = space.getMembers().stream()
                .sorted()
                .collect(Collectors.toList());

        return ListMapper.mapList(members, UnusUserOutputDTO.class);
    }

    private Space getSpace(String code) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        throwIfEmpty(storedSpace);
        return storedSpace.get();
    }

    private String getUniqueCode() {
        String code = "";
        do {
            code = CodeGenerator.get(8);
        } while(spaceRepository.findByCode(code).isPresent());
        return code;
    }

    private void throwIfEmpty(Optional<Space> space) {
        if (space.isEmpty()) {
            throw new ResourceNotFoundException("Space was not found.");
        }
    }

    private void throwIfAdmin(Space space, String email, String message) {
        if (space.getAdmin().getEmail().equals(email)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void throwIfNotAdmin(Space space, String email, String message) {
        if (!space.getAdmin().getEmail().equals(email)) {
            throw new IllegalArgumentException(message);
        }
    }

    private void throwIfNotMember(Space space, String email, String message) {
        if (!spaceRepository.isMember(space.getCode(), email)) {
            throw new IllegalArgumentException(message);
        }
    }
}
