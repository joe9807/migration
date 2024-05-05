package com.migration.cache;

import com.migration.configuration.AppConfig;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MigrationCache {
    private final AppConfig appConfig;
    private final List<MigrationMapper> mappers;
    private final Map<MigrationConfig, ConcurrentHashSet<MigrationObject, Boolean>> cacheObjects = new ConcurrentHashMap<>();

    @Getter
    private MigrationStatistics statistics;

    @Getter
    private ThreadPoolExecutor executor;

    private Map<MigrationType, MigrationMapper> cacheMappers;
    private volatile boolean isObjectsNotInCache;

    public void init(MigrationConfig config){
        isObjectsNotInCache = true;//because root object not in cache on start of migration or in case of resume
        statistics = new MigrationStatistics();
        executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(appConfig.getPoolSize(), getFactory());
        cacheMappers = mappers.stream().collect(Collectors.toMap(MigrationMapper::getMapperKey, Function.identity()));
        cacheObjects.put(config, new ConcurrentHashSet<>(Boolean.TRUE, appConfig.getCacheSize()));
    }

    public void populateCacheFromDatabase(MigrationConfig config, List<MigrationObject> objects){
        isObjectsNotInCache = !cacheObjects.get(config).addAllObjects(objects) || objects.size() == appConfig.getCacheSize();
    }

    public void populateCacheFromWorker(UUID configId, List<MigrationObject> objects){
        if (!cacheObjects.get(getConfig(configId)).addAllObjects(objects)){
            isObjectsNotInCache = true;
        }
    }

    public Set<MigrationObject> getObjects(MigrationConfig config){
        return cacheObjects.get(config).keySet();
    }

    public void evict(MigrationConfig config, Set<MigrationObject> objects){
        cacheObjects.get(config).keySet().removeAll(objects);
    }

    public boolean isNeedToUpdate(MigrationConfig config){
        return isEmpty(config) && isObjectsNotInCache;
    }

    public boolean isEmpty(MigrationConfig config){
        return cacheObjects.get(config).isEmpty();
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

    private ThreadFactory getFactory(){
        return new ThreadFactory(){
            final AtomicInteger count = new AtomicInteger(0);

            public Thread newThread(@NonNull Runnable r) {
                return new Thread(r, "migration-thread-"+count.getAndIncrement());
            }
        };
    }

    public String step(MigrationObjectStatus from, MigrationObjectStatus to, int value, String sourcePath, UUID configId){
        return statistics.step(from, to, value, cacheObjects.get(getConfig(configId)).size(), executor.getActiveCount(), executor.getQueue().size(), sourcePath);
    }
}
