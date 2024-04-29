package com.migration.cache;

import com.migration.configuration.MigrationConfig;
import com.migration.dto.MigrationStatistics;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.enums.MigrationType;
import com.migration.mapper.MigrationMapper;
import com.migration.repository.MigrationRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MigrationCache {
    private final List<MigrationMapper> mappers;
    private final MigrationRepository migrationRepository;
    private final Map<MigrationConfig, List<MigrationObject>> cacheObjects = new HashMap<>();

    @Getter
    private MigrationStatistics statistics;
    private Map<MigrationType, MigrationMapper> cacheMappers;
    private ThreadPoolExecutor executor;
    private SimpleDateFormat format;

    public void init(){
        statistics = new MigrationStatistics();
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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
        executor = null;
        statistics = null;
        cacheObjects.remove(config);
    }

    public MigrationConfig getConfig(UUID configId){
        return cacheObjects.keySet().stream().filter(config->config.getId().equals(configId)).findFirst().orElse(null);
    }

    public MigrationMapper getMigrationMapper(MigrationType migrationType){
        return cacheMappers.get(migrationType);
    }

    public ThreadPoolExecutor getExecutor(){
        if (executor == null ) executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        return executor;
    }

    public synchronized String step(MigrationObjectStatus from, MigrationObjectStatus to, int value, String sourcePath){
        statistics.step(from, to, value, sourcePath);

        return String.format("%-20s :: %-25s :: %6s :: %-15s :: %s"
                , format.format(new Date())
                , Thread.currentThread().getName()
                , executor != null?(executor.getActiveCount() + "(" + executor.getQueue().size() + ")"):""
                , statistics.getProcessed() + "/" + statistics.getTotal(),
                statistics.getSourcePath());
    }
}
