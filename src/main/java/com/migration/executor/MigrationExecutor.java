package com.migration.executor;

import com.migration.cache.MigrationCache;
import com.migration.configuration.AppConfig;
import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.repository.MigrationRepository;
import com.migration.service.MigrationService;
import com.migration.worker.MigrationWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class MigrationExecutor {
    private final MigrationRepository migrationRepository;
    private final AppConfig appConfig;
    private final MigrationCache migrationCache;
    private final MigrationService migrationService;

    public void start(MigrationConfig config, boolean resume){
        migrationCache.init();

        if (migrationCache.getMigrationMapper(config.getType()) == null){
            throw new RuntimeException("There is no corresponding mapper for "+config.getType()+" MigrationType. Please implement this mapper!");
        }

        if (!resume) {
            migrationRepository.save(MigrationObject.builder()
                    .type(config.getSourceContext().getInitObject().getType())
                    .sourceId(config.getSourceContext().getInitObject().getId())
                    .sourcePath(config.getSourceContext().getInitObject().getPath())
                    .targetPath(config.getTargetContext().getInitObject().getPath())
                    .status(MigrationObjectStatus.NEW)
                    .configId(config.getId())
                    .build()).toFuture().join();
        }

        run(config);

        migrationCache.finish(config);
    }

    public void run(MigrationConfig config){
        ThreadPoolExecutor migrationExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);

        long executedTasks = 0L;
        do {
            if (migrationExecutor.getQueue().isEmpty()) {
                executedTasks += executeTasks(migrationExecutor, config);
            }

            if (!isActive(migrationExecutor, executedTasks)) {
                List<MigrationObject> objects = migrationCache.getNewMigrationObjects(config);
                if (objects.size() == 0) break;
            }
        } while (true);
        log.info("migrationExecutor: {}; executedTasks: {}", migrationExecutor, executedTasks);
    }

    private boolean isActive(ThreadPoolExecutor migrationExecutor, long executedTasks){
        return migrationExecutor.getActiveCount() != 0 || !migrationExecutor.getQueue().isEmpty() || migrationExecutor.getCompletedTaskCount() != executedTasks;
    }

    private long executeTasks(ThreadPoolExecutor migrationExecutor, MigrationConfig config) {
        List<MigrationWorker> workers = migrationCache.getNewMigrationObjects(config)
                .stream()
                .limit(appConfig.getObjectsLimit())
                .map(object-> new MigrationWorker(object, migrationService))
                .toList();

        migrationRepository.capture(workers
                        .stream()
                        .map(MigrationWorker::getMigrationObject)
                        .peek(object->object.setStatus(MigrationObjectStatus.CAPTURED))
                        .map(MigrationObject::getId)
                        .collect(Collectors.toList())
                , MigrationObjectStatus.CAPTURED).toFuture().join();

        migrationCache.evict(config, workers.stream().map(MigrationWorker::getMigrationObject).toList());
        workers.forEach(migrationExecutor::execute);
        return workers.size();
    }
}
