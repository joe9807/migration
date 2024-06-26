package com.migration.context;

import com.migration.enums.MigrationObjectType;
import com.migration.object.FileSystemObject;
import com.migration.object.GenericObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSystemContext extends GenericContext {
    private String path;

    @Override
    public GenericObject getInitObject() {
        return FileSystemObject.builder().file(new File(path)).build();
    }

    @Override
    public GenericObject getObject(String id, String path, MigrationObjectType type) {
        FileSystemObject object = FileSystemObject.builder().file(new File(id == null?path:id)).build();
        object.setType(type);
        return object;
    }
}
