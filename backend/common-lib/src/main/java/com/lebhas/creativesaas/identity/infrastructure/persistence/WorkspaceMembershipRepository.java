package com.lebhas.creativesaas.identity.infrastructure.persistence;

import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipEntity;
import com.lebhas.creativesaas.identity.domain.WorkspaceMembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WorkspaceMembershipRepository extends JpaRepository<WorkspaceMembershipEntity, UUID> {

    Optional<WorkspaceMembershipEntity> findByUserIdAndWorkspaceIdAndDeletedFalse(UUID userId, UUID workspaceId);

    List<WorkspaceMembershipEntity> findAllByUserIdAndStatusAndDeletedFalse(UUID userId, WorkspaceMembershipStatus status);

    long countByUserIdAndStatusAndDeletedFalse(UUID userId, WorkspaceMembershipStatus status);

    boolean existsByUserIdAndWorkspaceIdAndStatusAndDeletedFalse(
            UUID userId,
            UUID workspaceId,
            WorkspaceMembershipStatus status
    );
}
