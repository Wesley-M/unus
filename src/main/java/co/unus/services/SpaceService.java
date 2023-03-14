package co.unus.services;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.SpaceOutputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.exceptions.SpaceNotFoundException;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.utils.CodeGenerator;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpaceService {
    private SpaceRepository spaceRepository;

    private UnusUserRepository userRepository;

    private final ModelMapper mapper = new ModelMapper();

    public SpaceService(SpaceRepository spaceRepository, UnusUserRepository userRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }

    public SpaceOutputDTO createSpace(SpaceInputDTO dto, String adminEmail) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(adminEmail);
        String code = getUniqueCode();
        UnusUser user = storedUser.get();
        Space space = new Space(dto.name(), code, dto.isPublic(), user);

        user.joinSpaceAsAdmin(space);
        userRepository.save(user);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public SpaceOutputDTO getSpaceByCode(String code) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        if (storedSpace.isEmpty()) {
            throw new SpaceNotFoundException(String.format("Space with code %s was not found.", code));
        }

        Space space = storedSpace.get();

        return mapper.map(space, SpaceOutputDTO.class);
    }

    public SpaceOutputDTO joinSpaceAsMember(String code, String email) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        if (storedSpace.isEmpty()) {
            throw new SpaceNotFoundException(String.format("Space with code %s was not found.", code));
        }

        Space space = storedSpace.get();

        if (space.getAdmin().getEmail().equals(email)) {
            throw new IllegalArgumentException("User is already the space admin.");
        }

        Optional<UnusUser> storedUser = userRepository.findByEmail(email);
        UnusUser user = storedUser.get();
        user.joinSpace(space);

        userRepository.save(user);
        spaceRepository.save(space);

        return mapper.map(space, SpaceOutputDTO.class);
    }

    private String getUniqueCode() {
        String code = "";
        do {
            code = CodeGenerator.get(8);
        } while(spaceRepository.findByCode(code).isPresent());
        return code;
    }
}
