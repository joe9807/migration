package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import com.migration.service.MigrationService;
import com.migration.utils.Utils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

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

    @PostMapping("/start")
    @Operation(summary = "Старт миграции по конфигу")
    public CompletionStage<List<String>> startMigration(@RequestBody MigrationConfig config){
        Date date = new Date();
        return migrationService.handle(config.getSourceContext().getInitObject()).thenApply(list->{
            log.info("Migration took {}", Utils.getTimeElapsed(new Date().getTime() - date.getTime()));
            return list;
        });
    }
}
