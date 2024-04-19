package com.migration.object;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class FakeObject extends GenericObject{
    private String id;
    private String name;
    private String path;

    @Override
    public GenericObject create() {
        return FakeObject.builder().id(UUID.randomUUID().toString()).path(path).build();
    }

    @Override
    public List<GenericObject> getChildren() {
        return new ArrayList<>();
    }

    @Override
    public String getType() {
        return "FAKE";
    }
}
