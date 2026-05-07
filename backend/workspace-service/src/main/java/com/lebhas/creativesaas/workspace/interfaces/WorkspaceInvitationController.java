package com.lebhas.creativesaas.workspace.interfaces;

import com.lebhas.creativesaas.common.api.ApiResponse;
import com.lebhas.creativesaas.identity.application.InvitationService;
import com.lebhas.creativesaas.identity.application.dto.InviteCrewCommand;
import com.lebhas.creativesaas.identity.application.dto.InvitationView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces")
@Tag(name = "Workspace Invitations")
@SecurityRequirement(name = "bearerAuth")
public class WorkspaceInvitationController {

    private final InvitationService invitationService;

    public WorkspaceInvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/{workspaceId}/invitations")
    @PreAuthorize("hasAuthority('CREW_INVITE')")
    @Operation(summary = "Invite a crew member to a workspace")
    public ApiResponse<InvitationView> inviteCrew(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateInvitationRequest request
    ) {
        return ApiResponse.success(invitationService.inviteCrew(new InviteCrewCommand(
                workspaceId,
                request.email(),
                request.role())));
    }
}
