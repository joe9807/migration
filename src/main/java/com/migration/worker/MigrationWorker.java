package com.migration.worker;

import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.mapper.MigrationMapper;
import com.migration.object.GenericObject;
import com.migration.repository.MigrationRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

@Slf4j
@Data
public class MigrationWorker implements Runnable {
    private MigrationMapper mapper;
    private MigrationRepository migrationRepository;
    private MigrationObject migrationObject;

    public MigrationWorker(MigrationObject migrationObject, MigrationRepository migrationRepository, MigrationMapper mapper){
        this.migrationObject = migrationObject;
        this.migrationRepository = migrationRepository;
        this.mapper = mapper;
    }

    @Override
    public void run() {
        try {
            GenericObject source = migrationObject.getSourceObject();
            GenericObject target = migrationObject.getTargetObject();
            mapper.map(target, source);

            if (target.getId() == null) {
                target = target.create();
            }

            if (target.getId() != null) {
                saveChildren(source, target);
            }
        } catch (Exception e){
            log.error("MigrationWorker error for object "+ migrationObject.getSourcePath(), e);
            failObject();
        } finally {
            //log.info("Object {} was migrated", migrationObject.getSourcePath());
        }
    }

    public void saveChildren(GenericObject source, GenericObject target){
        migrationRepository.saveAll(source.getChildren()
                .stream()
                .map(child -> MigrationObject.builder().sourceId(child.getId()).sourcePath(child.getPath()).status(MigrationObjectStatus.NEW)
                        .targetPath(target.getPath()+"/"+child.getName())
                        .type(child.getType())
                        .configId(migrationObject.getConfigId()).build())
                .collect(Collectors.toList())).subscribe();

        migrationObject.setTargetId(target.getId());
        migrationObject.setStatus(MigrationObjectStatus.DONE);
        migrationRepository.save(migrationObject).toFuture().join();
    }

    public void failObject(){
        migrationObject.setStatus(MigrationObjectStatus.FAILED);
        migrationRepository.save(migrationObject).toFuture().join();
    }
}
