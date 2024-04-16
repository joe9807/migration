package com.migration.worker;

import com.migration.entity.MigrationObject;
import com.migration.entity.MigrationObjectStatus;
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
            saveAll();
            //log.info("Object {} was migrated", migrationObject.getSourcePath());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Transactional
    public void saveAll(){
        migrationRepository.saveAll(migrationObject.getSourceObject().getChildren()
                .stream()
                .map(child -> MigrationObject.builder().sourcePath(child.getName()).status(MigrationObjectStatus.NEW).configId(migrationObject.getConfigId()).build())
                .collect(Collectors.toList()));

        migrationObject.setStatus(MigrationObjectStatus.DONE);
        migrationRepository.save(migrationObject);
    }
}
