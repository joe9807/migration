package com.migration.cache;

import com.migration.configuration.MigrationConfig;
import com.migration.dto.MigrationStatistics;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.enums.MigrationType;
import com.migration.mapper.MigrationMapper;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MigrationCache {
    private final List<MigrationMapper> mappers;
    private final Map<MigrationConfig, Set<MigrationObject>> cacheObjects = new ConcurrentHashMap<>();

    @Getter
    private MigrationStatistics statistics;
    private Map<MigrationType, MigrationMapper> cacheMappers;
    private ThreadPoolExecutor executor;

    public void init(MigrationConfig config){
        statistics = new MigrationStatistics();
        cacheMappers = mappers.stream().collect(Collectors.toMap(MigrationMapper::getMapperKey, Function.identity()));
        cacheObjects.put(config, ConcurrentHashMap.newKeySet());
    }

    public void populateCache(UUID configId, List<MigrationObject> objects){
        cacheObjects.get(getConfig(configId)).addAll(objects);
    }

    public Set<MigrationObject> getNewMigrationObjects(MigrationConfig config){
        return cacheObjects.get(config);
    }

    public void evict(MigrationConfig config, Set<MigrationObject> objects){
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
        if (executor == null ) executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50, getFactory());
        return executor;
    }

    private ThreadFactory getFactory(){
        return new ThreadFactory(){
            final AtomicInteger count = new AtomicInteger(0);

            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "migration-thread-"+count.getAndIncrement());
            }
        };
    }

    public synchronized String step(MigrationObjectStatus from, MigrationObjectStatus to, int value, String sourcePath){
        return statistics.step(from, to, value, executor != null?(executor.getActiveCount() + "(" + executor.getQueue().size() + ")"):"", sourcePath);
    }
}
