package com.migration.object;

import com.migration.context.FileSystemContext;
import com.migration.enums.MigrationObjectType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class FileSystemObject extends GenericObject {
    private FileSystemContext context;

    private File file;

    @Override
    public GenericObject create() {
        if (!file.exists()) {
            try {
                boolean result = type == MigrationObjectType.CONTAINER ? file.mkdirs() : file.createNewFile();
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return FileSystemObject.builder().file(file).build();
    }

    @Override
    public List<GenericObject> getChildren() {
        return Optional.ofNullable(file.listFiles())
                .<List<GenericObject>>map(files -> Stream.of(files).map(file -> FileSystemObject.builder().file(file).build()).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    @Override
    public String getId() {
        return file.exists()?file.getAbsolutePath():null;
    }

    @Override
    public String getPath() {
        return file.getAbsolutePath();
    }

    @Override
    public String getName() {
        return file.getName();
    }

    @Override
    public MigrationObjectType getType() {
        if (file.exists()) {
            return file.isDirectory()?MigrationObjectType.CONTAINER:MigrationObjectType.CONTENT;
        }

        return super.getType();
    }
}
