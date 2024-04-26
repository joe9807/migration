package com.migration.service;

import com.migration.cache.MigrationCache;
import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.mapper.MigrationMapper;
import com.migration.object.GenericObject;
import com.migration.repository.MigrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationService {
    private final MigrationRepository migrationRepository;
    private final MigrationCache migrationCache;

    public Flux<MigrationObject> getAllMigrationObjects(UUID configId, Long id){
        return migrationRepository.findByConfigIdOrderByIdAsc(configId, id);
    }

    public void deleteAll(){
        migrationRepository.deleteAll().toFuture().join();
    }

    public GenericObject getSourceObject(MigrationObject object){
        return migrationCache.getConfig(object.getConfigId()).getSourceContext().getObject(object.getSourceId(), object.getSourcePath(), object.getType());
    }

    public GenericObject getTargetObject(MigrationObject object){
        return migrationCache.getConfig(object.getConfigId()).getTargetContext().getObject(object.getSourceId(), object.getSourcePath(), object.getType());
    }

    public MigrationMapper getMigrationMapper(UUID configId){
        MigrationConfig config = migrationCache.getConfig(configId);
        return migrationCache.getMigrationMapper(config.getType());
    }

    public void failObject(MigrationObject migrationObject){
        migrationObject.setStatus(MigrationObjectStatus.FAILED);
        migrationRepository.save(migrationObject).toFuture().join();
    }

    public void saveChildren(MigrationObject migrationObject, GenericObject source, GenericObject target){
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
}
