package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import com.migration.service.MigrationService;
import com.migration.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.ForkJoinPool;

@RestController
@Slf4j
public class MigrationController {

    @Autowired
    private MigrationService migrationService;

    @GetMapping("/config")
    @Operation(summary = "Получить пример конфига миграции")
    public MigrationConfig getConfig(){
        return MigrationConfig.getConfigExample();
    }

    @PostMapping(value = "/start", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
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
}
