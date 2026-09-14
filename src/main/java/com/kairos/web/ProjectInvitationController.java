package com.kairos.web;

import com.kairos.dto.InvitationRequest;
import com.kairos.dto.InvitationResponse;
import com.kairos.service.ProjectInvitationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;

    public ProjectInvitationController(ProjectInvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/{projectId}/invitations")
    public ResponseEntity<Void> createInvitation(
            @PathVariable Long projectId,
            @Valid @RequestBody InvitationRequest request) {
        invitationService.createInvitation(projectId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/invitations/{token}")
    public ResponseEntity<InvitationResponse> getInvitationInfo(@PathVariable String token) {
        return ResponseEntity.ok(invitationService.getInvitationInfo(token));
    }

    @PostMapping("/invitations/{token}/accept")
    public ResponseEntity<Void> acceptInvitation(@PathVariable String token) {
        invitationService.acceptInvitation(token);
        return ResponseEntity.ok().build();
    }
}
