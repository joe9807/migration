package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import com.migration.executor.MigrationExecutor;
import com.migration.service.MigrationService;
import com.migration.service.MigrationServiceShort;
import com.migration.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
public class MigrationController {
    private final MigrationService migrationService;
    private final MigrationServiceShort migrationServiceShort;
    private final MigrationExecutor migrationExecutor;

    @GetMapping("/generate")
    @Operation(summary = "Получить пример конфига миграции")
    @Parameter(name = "configType", description = "configType", schema = @Schema(type = "string", allowableValues = {"FakeToFake", "FakeToFS", "FSToFake", "FSToFS"}))
    public MigrationConfig getConfig(String configType){
        return MigrationConfig.getConfigExample(configType);
    }

    @PostMapping(value = "/startFutures", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Старт миграции по конфигу")
    public Flux<String> startMigration(@RequestBody MigrationConfig config){
        Date date = new Date();
        return Mono.fromFuture(migrationServiceShort.handle(config.getSourceContext().getInitObject()).thenApply(list->{
            log.info("ForkJoinPool {}", ForkJoinPool.commonPool());
            log.info("Migration took {}", Utils.getTimeElapsed(new Date().getTime() - date.getTime()));
            return list;
        })).flatMapMany(Flux::fromStream);
    }

    @PostMapping(value = "/startFlux", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "Старт миграции по конфигу")
    public Flux<String> startFluxMigration(@RequestBody MigrationConfig config){
        Date date = new Date();
        return migrationServiceShort.handleFlux(config.getSourceContext().getInitObject()).doOnComplete(()->{
            log.info("ForkJoinPool {}", ForkJoinPool.commonPool());
            log.info("Migration took {}", Utils.getTimeElapsed(new Date().getTime() - date.getTime()));
        });
    }

    @PostMapping(value = "/startExecutor")
    @Operation(summary = "Старт миграции via Executor")
    @Parameter(name = "clearAll", description = "Очистить Все предыдущие миграции", schema = @Schema(type = "boolean", defaultValue = "false"))
    public String startExecutorMigration(@RequestBody MigrationConfig config, boolean clearAll){
        if (clearAll && config.getId() == null) {
            log.info("Clear previous migrations!");
            migrationService.deleteAll();
        }

        AtomicBoolean resume = new AtomicBoolean(false);
        if (config.getId() != null) {
            resume.set(true);
        } else {
            config.setId(UUID.randomUUID());
        }

        String url = String.format("http://localhost:5173/?configId=%s", config.getId());
        String result = String.format("%s migration %s with id: %s; %s; %s\n%s", resume.get()?"Resumed":"Started", LocalDateTime.now(), config.getId()
                , config.getSourceContext(), config.getTargetContext(), url);

        CompletableFuture.runAsync(() -> {
            Date date = new Date();
            log.info(result);
            migrationExecutor.start(config, resume.get());
            log.info("Time elapsed {}", Utils.getTimeElapsed(new Date().getTime()-date.getTime()));
        }).exceptionally(e->{
            log.error("Error during migration: ", e);
            return CompletableFuture.allOf().join();
        });
        return result;
    }

    @GetMapping(value = "/monitor")
    @Operation(summary = "Мониторинг процесса миграции")
    public Flux<ServerSentEvent<String>> getMigrationObjects(UUID configId, Long id){
        return migrationService.getAllMigrationObjects(configId, id).map(object-> ServerSentEvent.<String>builder()
                .id(String.valueOf(object.getId()))
                .event("message")
                .data(object.getSourcePath())
                .build());
    }

    @GetMapping(value = "/config")
    @Operation(summary = "Получение конфигурации по configId")
    public MigrationConfig getMigrationConfig(UUID configId){
        return migrationService.getMigrationConfig(configId);
    }
}
