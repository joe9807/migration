package com.migration.configuration;

import com.migration.context.FakeContext;
import com.migration.context.FileSystemContext;
import com.migration.context.GenericContext;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class MigrationConfig {
    private UUID id;
    private GenericContext sourceContext;
    private GenericContext targetContext;

    public static MigrationConfig getConfigExample(){
        String path = "D:\\DIFFERENT\\firefox\\";
        return MigrationConfig.builder()
                .sourceContext(FileSystemContext.builder().path(path).build())
                .targetContext(FakeContext.builder().path(path).build())
                .build();
    }
}
