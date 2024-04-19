package com.migration.worker;

import com.migration.entity.MigrationObject;
import com.migration.entity.MigrationObjectStatus;
import com.migration.object.GenericObject;
import com.migration.repository.MigrationRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Slf4j
@Data
public class MigrationWorker implements Runnable {
    private MigrationRepository migrationRepository;
    private MigrationObject migrationObject;

    public MigrationWorker(MigrationObject migrationObject, MigrationRepository migrationRepository){
        this.migrationObject = migrationObject;
        this.migrationRepository = migrationRepository;
    }

    @Override
    public void run() {
        try {
            GenericObject target = migrationObject.getTargetObject();
            if (target.getId() == null) {
                target = target.create();
            }

            if (target.getId() != null) {
                saveChildren(target);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    public void saveChildren(GenericObject target){
        migrationRepository.saveAll(migrationObject.getSourceObject().getChildren()
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
}
