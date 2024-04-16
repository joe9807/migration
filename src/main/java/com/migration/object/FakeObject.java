package com.migration.object;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
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
