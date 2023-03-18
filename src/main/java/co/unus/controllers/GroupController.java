package co.unus.controllers;

import co.unus.dtos.GroupInputDTO;
import co.unus.dtos.GroupOutputDTO;
import co.unus.services.GroupService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth/groups", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class GroupController {
    @Autowired
    private GroupService groupService;

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    GroupOutputDTO createGroup(@RequestBody @Valid GroupInputDTO dto, Authentication authentication) {
        return groupService.createGroup(dto, authentication.getName());
    }

    @DeleteMapping(value = "/{id}", consumes="*/*")
    @ResponseStatus(HttpStatus.OK)
    void removeGroup(@PathVariable("id") Long id, Authentication authentication) {
        groupService.removeGroup(id, authentication.getName());
    }
}
