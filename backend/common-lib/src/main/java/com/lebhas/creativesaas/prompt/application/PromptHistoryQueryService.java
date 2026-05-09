package com.lebhas.creativesaas.prompt.application;

import com.lebhas.creativesaas.common.api.PagedResult;
import com.lebhas.creativesaas.common.exception.BusinessException;
import com.lebhas.creativesaas.common.exception.ErrorCode;
import com.lebhas.creativesaas.common.security.Permission;
import com.lebhas.creativesaas.identity.application.WorkspaceAuthorizationService;
import com.lebhas.creativesaas.prompt.application.dto.PromptHistoryFilter;
import com.lebhas.creativesaas.prompt.application.dto.PromptHistoryView;
import com.lebhas.creativesaas.prompt.domain.PromptHistoryEntity;
import com.lebhas.creativesaas.prompt.infrastructure.persistence.PromptHistoryRepository;
import com.lebhas.creativesaas.prompt.infrastructure.persistence.PromptHistorySpecifications;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PromptHistoryQueryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final PromptHistoryRepository promptHistoryRepository;
    private final PromptViewMapper promptViewMapper;
    private final PromptActivityLogger promptActivityLogger;

    public PromptHistoryQueryService(
            WorkspaceAuthorizationService workspaceAuthorizationService,
            PromptHistoryRepository promptHistoryRepository,
            PromptViewMapper promptViewMapper,
            PromptActivityLogger promptActivityLogger
    ) {
        this.workspaceAuthorizationService = workspaceAuthorizationService;
        this.promptHistoryRepository = promptHistoryRepository;
        this.promptViewMapper = promptViewMapper;
        this.promptActivityLogger = promptActivityLogger;
    }

    @Transactional(readOnly = true)
    public PagedResult<PromptHistoryView> listHistory(PromptHistoryFilter filter) {
        requireHistoryViewAccess(filter.workspaceId(), "history_list");
        return PagedResult.from(promptHistoryRepository.findAll(
                        PromptHistorySpecifications.forList(filter),
                        PageRequest.of(
                                Math.max(filter.page(), 0),
                                Math.min(filter.size() <= 0 ? DEFAULT_PAGE_SIZE : filter.size(), MAX_PAGE_SIZE),
                                Sort.by(Sort.Direction.DESC, "createdAt")))
                .map(promptViewMapper::toHistoryView));
    }

    @Transactional(readOnly = true)
    public PromptHistoryView getHistory(UUID workspaceId, UUID historyId) {
        requireHistoryViewAccess(workspaceId, "history_get");
        PromptHistoryEntity entity = promptHistoryRepository.findByIdAndWorkspaceIdAndDeletedFalse(historyId, workspaceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PROMPT_HISTORY_NOT_FOUND));
        return promptViewMapper.toHistoryView(entity);
    }

    private void requireHistoryViewAccess(UUID workspaceId, String operation) {
        WorkspaceAuthorizationService.WorkspaceAccess access = workspaceAuthorizationService.requireWorkspaceContext(workspaceId);
        if (access.effectiveRole().isMaster()
                || access.permissions().contains(Permission.PROMPT_HISTORY_VIEW)
                || access.permissions().contains(Permission.PROMPT_TEMPLATE_MANAGE)) {
            return;
        }
        promptActivityLogger.logAuthorizationFailure(operation, workspaceId, access.currentUser().userId(), "missing_prompt_history_permission");
        throw new BusinessException(ErrorCode.FORBIDDEN);
    }
}
