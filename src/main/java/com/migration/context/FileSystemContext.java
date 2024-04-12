package com.migration.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.migration.object.FileSystemObject;
import com.migration.object.GenericObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSystemContext extends GenericContext {
    private String path;

    @Override
    public GenericObject getInitObject() {
        return FileSystemObject.builder().path(path).build();
    }
}
