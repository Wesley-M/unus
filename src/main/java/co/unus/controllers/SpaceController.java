package co.unus.controllers;

import co.unus.dtos.SpaceOutputDTO;
import co.unus.dtos.SpaceInputDTO;
import co.unus.services.SpaceService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping(value = "/spaces/{code}/join", consumes="*/*")
    @ResponseStatus(HttpStatus.CREATED)
    SpaceOutputDTO joinSpaceAsMember(@PathVariable("code") String code, Authentication authentication) {
        return spaceService.joinSpaceAsMember(code, authentication.getName());
    }
}
