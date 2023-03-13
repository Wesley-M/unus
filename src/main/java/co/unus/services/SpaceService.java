package co.unus.services;

import co.unus.daos.SpaceRepository;
import co.unus.daos.UnusUserRepository;
import co.unus.dtos.SpaceBaseDTO;
import co.unus.exceptions.SpaceNotFoundException;
import co.unus.models.Space;
import co.unus.models.UnusUser;
import co.unus.utils.CodeGenerator;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SpaceService {
    private SpaceRepository spaceRepository;

    private UnusUserRepository userRepository;

    public SpaceService(SpaceRepository spaceRepository, UnusUserRepository userRepository) {
        this.spaceRepository = spaceRepository;
        this.userRepository = userRepository;
    }

    public SpaceBaseDTO createSpace(String name, String adminEmail) {
        Optional<UnusUser> storedUser = userRepository.findByEmail(adminEmail);
        String code = getUniqueCode();
        UnusUser user = storedUser.get();
        Space space = new Space(name, code, user);

        user.joinSpaceAsAdmin(space);
        userRepository.save(user);

        return new SpaceBaseDTO(space.getName(), space.getCode(), space.getCreatedOn());
    }

    public SpaceBaseDTO getSpaceByCode(String code) {
        Optional<Space> storedSpace = spaceRepository.findByCode(code);
        if (storedSpace.isEmpty()) {
            throw new SpaceNotFoundException(String.format("Space with code %s was not found.", code));
        }

        Space space = storedSpace.get();
        return new SpaceBaseDTO(space.getName(), space.getCode(), space.getCreatedOn());
    }

    private String getUniqueCode() {
        String code = "";
        do {
            code = CodeGenerator.get(8);
        } while(spaceRepository.findByCode(code).isPresent());
        return code;
    }


}
