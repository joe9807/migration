package com.migration.cache;

import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.repository.MigrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Component
@RequiredArgsConstructor
public class MigrationCache {
    private final MigrationRepository migrationRepository;
    private final Map<MigrationConfig, List<MigrationObject>> cache = new HashMap<>();

    public List<MigrationObject> getNewMigrationObjects(MigrationConfig config){
        if (CollectionUtils.isEmpty(cache.get(config))){
            cache.put(config, migrationRepository.findByStatusAndConfigIdWithLimit(MigrationObjectStatus.NEW, config.getId()).collectList().toFuture().join());
        }
        return cache.get(config);
    }

    public void evict(MigrationConfig config, List<MigrationObject> objects){
        cache.get(config).removeAll(objects);
    }
}
