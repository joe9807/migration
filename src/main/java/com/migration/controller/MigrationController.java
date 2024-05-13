package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import com.migration.executor.MigrationExecutor;
import com.migration.service.MigrationService;
import com.migration.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin
public class MigrationController {
    private final MigrationService migrationService;
    private final MigrationExecutor migrationExecutor;

    @GetMapping("/generate")
    @Operation(summary = "Получить пример конфига миграции")
    @Parameter(name = "configType", description = "configType", schema = @Schema(type = "string", allowableValues = {"FakeToFake", "FakeToFS", "FSToFake", "FSToFS"}))
    public MigrationConfig getConfig(String configType){
        return MigrationConfig.getConfigExample(configType);
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

    @GetMapping(value = "/config")
    @Operation(summary = "Получение конфигурации по configId")
    public MigrationConfig getMigrationConfig(UUID configId){
        return migrationService.getMigrationConfig(configId);
    }
}
