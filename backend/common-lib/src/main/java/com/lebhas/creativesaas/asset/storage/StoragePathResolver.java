package com.lebhas.creativesaas.asset.storage;

import com.lebhas.creativesaas.asset.domain.AssetCategory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Component
public class StoragePathResolver {

    private final Clock clock;

    public StoragePathResolver(Clock clock) {
        this.clock = clock;
    }

    public String resolve(UUID workspaceId, AssetCategory category, UUID assetId, String storedFileName) {
        LocalDate today = LocalDate.now(clock);
        return "workspaces/%s/assets/%s/%d/%02d/%s-%s".formatted(
                workspaceId,
                category.name().toLowerCase(Locale.ROOT),
                today.getYear(),
                today.getMonthValue(),
                assetId,
                storedFileName);
    }
}
