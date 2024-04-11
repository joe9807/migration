package com.migration.controller;

import com.migration.configuration.MigrationConfig;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MigrationController {

    @GetMapping("/config")
    @Operation(summary = "Получить пример конфига миграции")
    public MigrationConfig getConfig(){
        return MigrationConfig.getConfigExample();
    }

    @PostMapping("/start")
    @Operation(summary = "Старт миграции по конфигу")
    public String startMigration(@RequestBody MigrationConfig config){
        return "Migration started";
    }
}
