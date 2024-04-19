package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.service.MigrationService;
import com.migration.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MigrationController {
    private final MigrationService migrationService;

    @GetMapping("/config")
    @Operation(summary = "Получить пример конфига миграции")
    public MigrationConfig getConfig(){
        return MigrationConfig.getConfigExample();
    }

    @PostMapping(value = "/startFutures", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Старт миграции по конфигу")
    public Flux<String> startMigration(@RequestBody MigrationConfig config){
        Date date = new Date();
        return Mono.fromFuture(migrationService.handle(config.getSourceContext().getInitObject()).thenApply(list->{
            log.info("ForkJoinPool {}", ForkJoinPool.commonPool());
            log.info("Migration took {}", Utils.getTimeElapsed(new Date().getTime() - date.getTime()));
            return list;
        })).flatMapMany(Flux::fromStream);
    }

    @PostMapping(value = "/startFlux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Старт миграции по конфигу")
    public Flux<String> startFluxMigration(@RequestBody MigrationConfig config){
        Date date = new Date();
        return migrationService.handleFlux(config.getSourceContext().getInitObject()).doOnComplete(()->{
            log.info("ForkJoinPool {}", ForkJoinPool.commonPool());
            log.info("Migration took {}", Utils.getTimeElapsed(new Date().getTime() - date.getTime()));
        });
    }

    @PostMapping(value = "/startExecutor")
    @Operation(summary = "Старт миграции via Executor")
    public String startExecutorMigration(@RequestBody MigrationConfig config, Integer count){
        List<UUID> ids = IntStream.range(0, count).mapToObj(index->UUID.randomUUID()).collect(Collectors.toList());;
        CompletableFuture.runAsync(() -> {
            IntStream.range(0, count).forEach(index->{
                config.setId(ids.get(index));
                Date date = new Date();
                log.info("Started migration with configId: {}", config.getId());
                migrationService.handleExecutor(config);
                log.info("{}: Time elapsed {}", index, Utils.getTimeElapsed(new Date().getTime()-date.getTime()));
            });
            log.info("----------------");
        });
        return String.format("Migrations triggered. Config Ids: %s", ids.stream().map(UUID::toString).collect(Collectors.joining(", ")));
    }

    @GetMapping(value = "/monitor", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Мониторинг процесса миграции")
    public Flux<String> getMigrationObjects(UUID configId){
        return migrationService.getAllMigrationObjects(configId).map(MigrationObject::getSourcePath);
    }
}
