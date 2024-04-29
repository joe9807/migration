package com.migration.worker;

import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.object.GenericObject;
import com.migration.service.MigrationService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class MigrationWorker implements Runnable {
    private MigrationObject migrationObject;
    private MigrationService migrationService;

    public MigrationWorker(MigrationObject migrationObject, MigrationService migrationService){
        this.migrationObject = migrationObject;
        this.migrationService = migrationService;
    }

    @Override
    public void run() {
        try {
            GenericObject source = migrationService.getSourceObject(migrationObject);
            GenericObject target = migrationService.getTargetObject(migrationObject);
            migrationService.getMigrationMapper(migrationObject.getConfigId()).map(target, source);

            if (target.getId() == null) {
                target = target.create();
            }

            if (target.getId() != null) {
                migrationObject.setTargetId(target.getId());
                migrationService.saveChildren(migrationObject, source, target);
            }
        } catch (Exception e){
            log.error("MigrationWorker error for object "+ migrationObject.getSourcePath(), e);
            migrationService.failObject(migrationObject);
        } finally {
            System.out.println(migrationService.complete(migrationObject, MigrationObjectStatus.DONE));
        }
    }
}