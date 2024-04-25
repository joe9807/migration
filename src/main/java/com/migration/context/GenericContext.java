package com.migration.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.migration.object.GenericObject;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.CLASS,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileSystemContext.class),
        @JsonSubTypes.Type(value = FakeContext.class)
})
public abstract class GenericContext {
    @JsonIgnore
    public abstract GenericObject getInitObject();

    @JsonIgnore
    public abstract GenericObject getObject(String id, String path, String type);
}
