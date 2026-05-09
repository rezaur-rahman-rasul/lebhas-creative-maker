package com.lebhas.creativesaas.creative.interfaces;

import com.lebhas.creativesaas.approval.application.CreativeApprovalService;
import com.lebhas.creativesaas.approval.application.dto.AddCreativeReviewCommentCommand;
import com.lebhas.creativesaas.approval.application.dto.CreateCreativeApprovalCommand;
import com.lebhas.creativesaas.approval.application.dto.CreativeApprovalHistoryView;
import com.lebhas.creativesaas.approval.application.dto.CreativeApprovalListCriteria;
import com.lebhas.creativesaas.approval.application.dto.CreativeApprovalView;
import com.lebhas.creativesaas.approval.application.dto.CreativeReviewCommentView;
import com.lebhas.creativesaas.common.api.ApiResponse;
import com.lebhas.creativesaas.common.api.PagedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/creative-approvals")
@Tag(name = "Creative Approvals")
@SecurityRequirement(name = "bearerAuth")
public class CreativeApprovalController {

    private final CreativeApprovalService creativeApprovalService;

    public CreativeApprovalController(CreativeApprovalService creativeApprovalService) {
        this.creativeApprovalService = creativeApprovalService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(
            summary = "Create a draft creative approval",
            description = "Creates a workspace-scoped approval workflow for a generated creative output. Duplicate active approvals for the same output are rejected."
    )
    public ApiResponse<CreativeApprovalView> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateCreativeApprovalRequest request
    ) {
        return ApiResponse.success(creativeApprovalService.create(new CreateCreativeApprovalCommand(
                workspaceId,
                request.creativeOutputId(),
                request.priority(),
                request.dueAt(),
                request.approvalNote())));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(
            summary = "List creative approvals",
            description = "Crew users see their own approvals. Admin and Master users can review the workspace queue."
    )
    public ApiResponse<PagedResult<CreativeApprovalView>> list(
            @PathVariable UUID workspaceId,
            @Valid @ModelAttribute CreativeApprovalListRequest request
    ) {
        return ApiResponse.success(creativeApprovalService.list(new CreativeApprovalListCriteria(
                workspaceId,
                request.getCreativeOutputId(),
                request.getGenerationRequestId(),
                request.getSubmittedBy(),
                request.getReviewedBy(),
                request.getStatus(),
                request.getPriority(),
                request.getPage() == null ? 0 : request.getPage(),
                request.getSize() == null ? 20 : request.getSize())));
    }

    @GetMapping("/{approvalId}")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Get a creative approval")
    public ApiResponse<CreativeApprovalView> get(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success(creativeApprovalService.get(workspaceId, approvalId));
    }

    @PostMapping("/{approvalId}/submit")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Submit a creative for review", description = "Allowed transitions: DRAFT -> SUBMITTED and REGENERATE_REQUESTED -> SUBMITTED.")
    public ApiResponse<CreativeApprovalView> submit(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success("Creative submitted for review", creativeApprovalService.submit(workspaceId, approvalId));
    }

    @PostMapping("/{approvalId}/start-review")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Start admin review", description = "Admin or Master only. Allowed transition: SUBMITTED -> IN_REVIEW.")
    public ApiResponse<CreativeApprovalView> startReview(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success("Creative review started", creativeApprovalService.startReview(workspaceId, approvalId));
    }

    @PostMapping("/{approvalId}/approve")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Approve a creative", description = "Admin or Master only. Allowed transition: IN_REVIEW -> APPROVED.")
    public ApiResponse<CreativeApprovalView> approve(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId,
            @Valid @RequestBody(required = false) ApproveCreativeApprovalRequest request
    ) {
        return ApiResponse.success("Creative approved", creativeApprovalService.approve(
                workspaceId,
                approvalId,
                request == null ? null : request.approvalNote()));
    }

    @PostMapping("/{approvalId}/reject")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Reject a creative", description = "Admin or Master only. Rejection reason is required. Allowed transition: IN_REVIEW -> REJECTED.")
    public ApiResponse<CreativeApprovalView> reject(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId,
            @Valid @RequestBody RejectCreativeApprovalRequest request
    ) {
        return ApiResponse.success("Creative rejected", creativeApprovalService.reject(
                workspaceId,
                approvalId,
                request.rejectionReason()));
    }

    @PostMapping("/{approvalId}/request-regenerate")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Request regeneration", description = "Admin or Master only. Regenerate instruction is required. Allowed transition: IN_REVIEW -> REGENERATE_REQUESTED.")
    public ApiResponse<CreativeApprovalView> requestRegenerate(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId,
            @Valid @RequestBody RequestRegenerateApprovalRequest request
    ) {
        return ApiResponse.success("Creative regeneration requested", creativeApprovalService.requestRegenerate(
                workspaceId,
                approvalId,
                request.regenerateInstruction()));
    }

    @PostMapping("/{approvalId}/cancel")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Cancel an approval", description = "Creator, Admin, or Master can cancel. Allowed transitions: SUBMITTED -> CANCELLED and IN_REVIEW -> CANCELLED.")
    public ApiResponse<CreativeApprovalView> cancel(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success("Creative approval cancelled", creativeApprovalService.cancel(workspaceId, approvalId));
    }

    @PostMapping("/{approvalId}/comments")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "Add a review comment", description = "Comment length must be between 2 and 2000 characters.")
    public ApiResponse<CreativeReviewCommentView> addComment(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId,
            @Valid @RequestBody AddCreativeReviewCommentRequest request
    ) {
        return ApiResponse.success(creativeApprovalService.addComment(new AddCreativeReviewCommentCommand(
                workspaceId,
                approvalId,
                request.comment(),
                request.commentType())));
    }

    @GetMapping("/{approvalId}/comments")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "List review comments")
    public ApiResponse<List<CreativeReviewCommentView>> comments(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success(creativeApprovalService.comments(workspaceId, approvalId));
    }

    @GetMapping("/{approvalId}/history")
    @PreAuthorize("hasAuthority('CREATIVE_SUBMIT')")
    @Operation(summary = "List approval review history")
    public ApiResponse<List<CreativeApprovalHistoryView>> history(
            @PathVariable UUID workspaceId,
            @PathVariable UUID approvalId
    ) {
        return ApiResponse.success(creativeApprovalService.history(workspaceId, approvalId));
    }
}
