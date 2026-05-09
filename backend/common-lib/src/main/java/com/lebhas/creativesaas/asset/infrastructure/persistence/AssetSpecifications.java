package com.lebhas.creativesaas.asset.infrastructure.persistence;

import com.lebhas.creativesaas.asset.application.dto.AssetListCriteria;
import com.lebhas.creativesaas.asset.domain.AssetEntity;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.Instant;

public final class AssetSpecifications {

    private AssetSpecifications() {
    }

    public static Specification<AssetEntity> forList(AssetListCriteria criteria) {
        return notDeleted()
                .and(hasWorkspace(criteria.workspaceId()))
                .and(hasCategory(criteria))
                .and(hasFileType(criteria))
                .and(hasFolder(criteria))
                .and(hasUploadedBy(criteria))
                .and(hasStatus(criteria))
                .and(hasTag(criteria))
                .and(hasKeyword(criteria))
                .and(createdFrom(criteria))
                .and(createdTo(criteria));
    }

    private static Specification<AssetEntity> notDeleted() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("deleted"));
    }

    private static Specification<AssetEntity> hasWorkspace(java.util.UUID workspaceId) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("workspaceId"), workspaceId);
    }

    private static Specification<AssetEntity> hasCategory(AssetListCriteria criteria) {
        return criteria.assetCategory() == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("assetCategory"), criteria.assetCategory());
    }

    private static Specification<AssetEntity> hasFileType(AssetListCriteria criteria) {
        return criteria.fileType() == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("fileType"), criteria.fileType());
    }

    private static Specification<AssetEntity> hasFolder(AssetListCriteria criteria) {
        return criteria.folderId() == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("folderId"), criteria.folderId());
    }

    private static Specification<AssetEntity> hasUploadedBy(AssetListCriteria criteria) {
        return criteria.uploadedBy() == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("uploadedBy"), criteria.uploadedBy());
    }

    private static Specification<AssetEntity> hasStatus(AssetListCriteria criteria) {
        return criteria.status() == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), criteria.status());
    }

    private static Specification<AssetEntity> hasTag(AssetListCriteria criteria) {
        if (!StringUtils.hasText(criteria.tag())) {
            return null;
        }
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            Expression<String> tagJoin = root.join("tags", JoinType.LEFT).as(String.class);
            return criteriaBuilder.equal(criteriaBuilder.lower(tagJoin), criteria.tag().trim().toLowerCase());
        };
    }

    private static Specification<AssetEntity> hasKeyword(AssetListCriteria criteria) {
        if (!StringUtils.hasText(criteria.keyword())) {
            return null;
        }
        String keyword = "%" + criteria.keyword().trim().toLowerCase() + "%";
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(
                criteriaBuilder.lower(root.get("originalFileName")),
                keyword);
    }

    private static Specification<AssetEntity> createdFrom(AssetListCriteria criteria) {
        Instant createdFrom = criteria.createdFrom();
        return createdFrom == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), createdFrom);
    }

    private static Specification<AssetEntity> createdTo(AssetListCriteria criteria) {
        Instant createdTo = criteria.createdTo();
        return createdTo == null ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), createdTo);
    }
}
