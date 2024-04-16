package com.migration.context;

import com.migration.object.FakeObject;
import com.migration.object.GenericObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FakeContext extends GenericContext {
    private String path;

    @Override
    public GenericObject getInitObject() {
        return FakeObject.builder().path(path).build();
    }
}
