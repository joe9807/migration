package com.migration.cache;

import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.enums.MigrationType;
import com.migration.mapper.MigrationMapper;
import com.migration.repository.MigrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MigrationCache {
    private final List<MigrationMapper> mappers;
    private final MigrationRepository migrationRepository;
    private Map<MigrationConfig, List<MigrationObject>> cacheObjects;
    private Map<MigrationType, MigrationMapper> cacheMappers;

    public void init(){
        cacheObjects = new HashMap<>();
        cacheMappers = mappers.stream().collect(Collectors.toMap(MigrationMapper::getMapperKey, Function.identity()));
    }

    public List<MigrationObject> getNewMigrationObjects(MigrationConfig config){
        if (CollectionUtils.isEmpty(cacheObjects.get(config))){
            cacheObjects.put(config, migrationRepository.findByStatusAndConfigIdWithLimit(MigrationObjectStatus.NEW, config.getId()).collectList().toFuture().join());
        }
        return cacheObjects.get(config);
    }

    public void evict(MigrationConfig config, List<MigrationObject> objects){
        cacheObjects.get(config).removeAll(objects);
    }

    public void finish(MigrationConfig config){
        cacheObjects.remove(config);
    }

    public MigrationConfig getConfig(UUID configId){
        return cacheObjects.keySet().stream().filter(config->config.getId().equals(configId)).findFirst().orElse(null);
    }

    public MigrationMapper getMigrationMapper(MigrationType migrationType){
        return cacheMappers.get(migrationType);
    }
}