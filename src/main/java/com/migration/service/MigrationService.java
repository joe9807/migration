package com.migration.service;

import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.entity.MigrationObjectStatus;
import com.migration.object.GenericObject;
import com.migration.repository.MigrationRepository;
import com.migration.worker.MigrationWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationService {
    private final MigrationRepository migrationRepository;

    public CompletableFuture<Stream<String>> handle(GenericObject object){
        CompletableFuture<Stream<String>> futureObject = CompletableFuture.completedFuture(object).thenApplyAsync(this::getWorkerMethod);
        for (GenericObject child:object.getChildren()){
            futureObject = futureObject.thenCombineAsync(handle(child), Stream::concat);
        }

        return futureObject;
    }

    public Flux<String> handleFlux(GenericObject object){
        Flux<String> fluxObject = Flux.just(object).map(this::getWorkerMethod).flatMap(Flux::fromStream);
        for (GenericObject child:object.getChildren()){
            fluxObject = fluxObject.mergeWith(handleFlux(child));
        }

        return fluxObject;
    }

    private Stream<String> getWorkerMethod(GenericObject object){
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.printf("%-40s: %s%n", Thread.currentThread().getName(), object.getPath());
        return Stream.of(object.getPath());
    }

    public void handleExecutor(MigrationConfig config){
        migrationRepository.save(MigrationObject.builder()
                .type(config.getSourceContext().getInitObject().getType())
                .sourceId(config.getSourceContext().getInitObject().getId())
                .sourcePath(config.getSourceContext().getInitObject().getPath())
                .targetPath(config.getTargetContext().getInitObject().getPath())
                .status(MigrationObjectStatus.NEW)
                .configId(config.getId())
                .build()).toFuture().join();
        run(config.getId());
    }

    public void run(UUID configId){
        ThreadPoolExecutor migrationExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(50);
        long executedTasks = 0L;

        do {
            if (migrationExecutor.getQueue().isEmpty()) {
                executedTasks += executeTasks(migrationExecutor, configId);
            }

            if (!isActive(migrationExecutor, executedTasks)) {
                List<MigrationObject> objects = getNewMigrationObjects(configId);
                if (objects.size() == 0) break;
            }
        } while (true);
        log.info("migrationExecutor: {}; executedTasks: {}", migrationExecutor, executedTasks);
    }

    private boolean isActive(ThreadPoolExecutor migrationExecutor, long executedTasks){
        return migrationExecutor.getActiveCount() != 0 || !migrationExecutor.getQueue().isEmpty() || migrationExecutor.getCompletedTaskCount() != executedTasks;
    }

    private List<MigrationObject> getNewMigrationObjects(UUID configId){
        return migrationRepository.findByStatusAndConfigId(MigrationObjectStatus.NEW, configId).collectList().toFuture().join();
    }

    private long executeTasks(ThreadPoolExecutor migrationExecutor, UUID configId) {
        List<MigrationWorker> workers = getNewMigrationObjects(configId)
                .stream()
                .map(object-> new MigrationWorker(object, migrationRepository))
                .collect(Collectors.toList());

        migrationRepository.capture(workers
                .stream()
                .map(MigrationWorker::getMigrationObject)
                .peek(object->object.setStatus(MigrationObjectStatus.CAPTURED))
                .map(MigrationObject::getId)
                .collect(Collectors.toList())
                , MigrationObjectStatus.CAPTURED).toFuture().join();

        workers.forEach(migrationExecutor::execute);
        return workers.size();
    }

    public Flux<MigrationObject> getAllMigrationObjects(UUID configId){
        return migrationRepository.findByConfigId(configId);
    }
}
