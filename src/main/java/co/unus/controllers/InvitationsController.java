package co.unus.controllers;

import co.unus.dtos.InvitationInputDTO;
import co.unus.models.Invitation;
import co.unus.services.InvitationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/auth/invitations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class InvitationsController {
    @Autowired
    private InvitationService invitationService;

    @PostMapping(value = "")
    @ResponseStatus(HttpStatus.CREATED)
    Invitation invite(@RequestBody @Valid InvitationInputDTO dto, Authentication authentication) {
        return invitationService.invite(dto, authentication.getName());
    }

    @DeleteMapping(value = "/{invitationId}")
    @ResponseStatus(HttpStatus.OK)
    Invitation removeInvitation(@PathVariable("invitationId") Long invitationId, Authentication authentication) {
        return invitationService.removeInvitation(invitationId, authentication.getName());
    }
}
