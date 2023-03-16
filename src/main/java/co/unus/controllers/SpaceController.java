package co.unus.controllers;

import co.unus.dtos.SpaceOutputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.dtos.UnusUserOutputDTO;
import co.unus.services.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/auth", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class SpaceController {

    @Autowired
    private SpaceService spaceService;

    @PostMapping(value = "/spaces")
    @ResponseStatus(HttpStatus.CREATED)
    SpaceOutputDTO createSpace(@RequestBody @Valid SpaceInputDTO dto, Authentication authentication) {
        return spaceService.createSpace(dto, authentication.getName());
    }

    @GetMapping(value = "/spaces/{code}", consumes="*/*")
    @ResponseStatus(HttpStatus.OK)
    SpaceOutputDTO getSpaceByCode(@PathVariable("code") String code) {
        return spaceService.getSpaceByCode(code);
    }

    @DeleteMapping(value = "/spaces/{code}", consumes="*/*")
    @ResponseStatus(HttpStatus.CREATED)
    void removeSpace(@PathVariable("code") String code, Authentication authentication) {
        spaceService.removeSpace(code, authentication.getName());
    }

    @PostMapping(value = "/spaces/{code}/join", consumes="*/*")
    @ResponseStatus(HttpStatus.CREATED)
    SpaceOutputDTO joinSpaceAsMember(@PathVariable("code") String code, Authentication authentication) {
        return spaceService.joinSpaceAsMember(code, authentication.getName());
    }

    @DeleteMapping(value = "/spaces/{code}/leave", consumes="*/*")
    @ResponseStatus(HttpStatus.CREATED)
    void leaveSpaceAsMember(@PathVariable("code") String code, Authentication authentication) {
        spaceService.leaveSpaceAsMember(code, authentication.getName());
    }

    @GetMapping(value = "/spaces/{code}/members", consumes="*/*")
    @ResponseStatus(HttpStatus.OK)
    List<UnusUserOutputDTO> getMembers(@PathVariable("code") String code, Authentication authentication) {
        return spaceService.getMembers(code, authentication.getName());
    }
}
