package com.migration.context;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FileSystemContext.class),
        @JsonSubTypes.Type(value = FakeContext.class)
})
public class GenericContext {
}
