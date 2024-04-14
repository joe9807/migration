package com.migration.object;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class FakeObject extends GenericObject{
    private String path;

    @Override
    public GenericObject create() {
        return FakeObject.builder().build();
    }

    @Override
    public List<GenericObject> getChildren() {
        return null;
    }

    @Override
    public String getName() {
        return null;
    }
}
