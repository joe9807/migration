package com.migration.configuration;

import com.migration.context.FakeContext;
import com.migration.context.FileSystemContext;
import com.migration.context.GenericContext;
import com.migration.enums.MigrationType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(of = {"id"})
public class MigrationConfig {
    private UUID id;
    private MigrationType type;
    private GenericContext sourceContext;
    private GenericContext targetContext;

    public static MigrationConfig getConfigExample(String configType){
        return switch (MigrationType.valueOf(configType)) {
            case FakeToFake -> getFakeToFakeExample();
            case FakeToFS -> getFakeToFSExample();
            case FSToFake -> getFSToFakeExample();
            case FSToFS -> getFSToFSExample();
        };
    }

    private static MigrationConfig getFakeToFakeExample(){
        return MigrationConfig.builder()
                .type(MigrationType.FakeToFake)
                .sourceContext(FakeContext.getFakeContextExample())
                .targetContext(FakeContext.builder().build())
                .build();
    }

    private static MigrationConfig getFSToFakeExample(){
        String path = "D:\\DIFFERENT\\firefox\\";

        return MigrationConfig.builder()
                .type(MigrationType.FSToFake)
                .sourceContext(FileSystemContext.builder().path(path).build())
                .targetContext(FakeContext.builder().build())
                .build();
    }

    private static MigrationConfig getFakeToFSExample(){
        String path = "E:\\DIFFERENT\\firefox\\";

        return MigrationConfig.builder()
                .type(MigrationType.FakeToFS)
                .sourceContext(FakeContext.getFakeContextExample())
                .targetContext(FileSystemContext.builder().path(path).build())
                .build();
    }

    private static MigrationConfig getFSToFSExample(){
        String sourcePath = "D:\\DIFFERENT\\firefox\\";
        String targetContext = "E:\\DIFFERENT\\firefox\\";

        return MigrationConfig.builder()
                .type(MigrationType.FSToFS)
                .sourceContext(FileSystemContext.builder().path(sourcePath).build())
                .targetContext(FileSystemContext.builder().path(targetContext).build())
                .build();
    }
}
