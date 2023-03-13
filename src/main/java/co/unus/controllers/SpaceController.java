package co.unus.controllers;

import co.unus.dtos.SpaceBaseDTO;
import co.unus.dtos.SpaceSimpleDTO;
import co.unus.models.Space;
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
    SpaceBaseDTO createSpace(@RequestBody @Valid SpaceSimpleDTO dto, Authentication authentication) {
        return spaceService.createSpace(dto.name(), authentication.getName());
    }

    @GetMapping(value = "/spaces/{code}", consumes="*/*")
    @ResponseStatus(HttpStatus.OK)
    SpaceBaseDTO getSpaceByCode(@PathVariable("code") String code) {
        return spaceService.getSpaceByCode(code);
    }
//
//    void updateSpace() {
//
//    }
//
//    void deleteSpace() {
//
//    }
}
