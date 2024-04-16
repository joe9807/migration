package com.migration.object;

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
    private String path;

    @Override
    public GenericObject create() {
        return null;
    }

    @Override
    public List<GenericObject> getChildren() {
        return Optional.ofNullable(new File(path).listFiles())
                .<List<GenericObject>>map(files -> Stream.of(files).map(file -> FileSystemObject.builder().path(file.getAbsolutePath()).build()).collect(Collectors.toList()))
                .orElseGet(ArrayList::new);
    }

    @Override
    public String getName() {
        return path;
    }
}
