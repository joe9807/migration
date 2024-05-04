package com.migration.executor;

import com.migration.cache.MigrationCache;
import com.migration.configuration.AppConfig;
import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.service.MigrationService;
import com.migration.worker.MigrationWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationExecutor {
    private final AppConfig appConfig;
    private final MigrationCache migrationCache;
    private final MigrationService migrationService;

    public void start(MigrationConfig config, boolean resume){
        migrationCache.init(config);

        if (migrationCache.getMigrationMapper(config.getType()) == null){
            throw new RuntimeException("There is no corresponding mapper for "+config.getType()+" MigrationType. Please implement this mapper!");
        }

        if (!resume) {
            MigrationObject rootObject = migrationService.createRootObject(config);
            migrationCache.step(null, rootObject.getStatus(), 1, rootObject.getSourcePath(), config.getId());
        }

        run(config);

        migrationCache.finish(config);
    }

    public void run(MigrationConfig config){
        long executedTasks = 0L;
        long completedTasks;

        do {
            completedTasks = migrationCache.getExecutor().getCompletedTaskCount();
            if (migrationCache.getExecutor().getQueue().size()<100) {
                executedTasks += executeTasks(config);
            }

            migrationService.sendStatistics();
        } while (completedTasks != executedTasks);

        migrationService.sendStatistics();
        log.info("migrationExecutor: {}; executedTasks: {}", migrationCache.getExecutor(), executedTasks);
    }

    private long executeTasks(MigrationConfig config) {
        Set<MigrationObject> objects = migrationCache.getNewMigrationObjects(config).stream()
                .limit(appConfig.getObjectsLimit()).collect(Collectors.toSet());

        migrationService.capture(objects.stream()
                .map(MigrationObject::getId)
                .collect(Collectors.toList()));

        migrationCache.evict(config, objects);
        migrationCache.step(MigrationObjectStatus.NEW, MigrationObjectStatus.CAPTURED, objects.size(), null, config.getId());
        objects.forEach(object-> migrationCache.getExecutor().execute(new MigrationWorker(object, migrationService)));
        return objects.size();
    }
}
