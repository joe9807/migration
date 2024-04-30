package com.migration.service;

import com.migration.cache.MigrationCache;
import com.migration.configuration.MigrationConfig;
import com.migration.entity.MigrationObject;
import com.migration.enums.MigrationObjectStatus;
import com.migration.mapper.MigrationMapper;
import com.migration.object.GenericObject;
import com.migration.repository.MigrationRepository;
import com.migration.websocket.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MigrationService {
    private final MigrationRepository migrationRepository;
    private final MigrationCache migrationCache;
    private final WebSocketService webSocketService;

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
        List<GenericObject> children = source.getChildren();
        saveAll(children.stream()
                .map(child -> MigrationObject.builder().sourceId(child.getId()).sourcePath(child.getPath()).status(MigrationObjectStatus.NEW)
                        .targetPath(target.getPath()+"/"+child.getName())
                        .type(child.getType())
                        .configId(migrationObject.getConfigId()).build())
                .collect(Collectors.toList()));

        migrationCache.step(null, MigrationObjectStatus.NEW, children.size(), null);
    }

    private void saveAll(List<MigrationObject> objects){
        migrationRepository.saveAll(objects).collectList().toFuture().join();
    }

    public MigrationConfig getMigrationConfig(UUID configId){
        return migrationCache.getConfig(configId);
    }

    public void complete(MigrationObject migrationObject, MigrationObjectStatus to){
        MigrationObjectStatus from = migrationObject.getStatus();
        migrationObject.setStatus(to);
        migrationRepository.save(migrationObject).toFuture().join();
        String result = migrationCache.step(from, to, 1, migrationObject.getSourcePath());
        log.info(result);
        webSocketService.sendMessageToClient(result);
        webSocketService.sendMessageToClient(migrationCache.getStatistics());
    }
}
