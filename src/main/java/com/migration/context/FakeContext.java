package com.migration.context;

import com.migration.object.GenericObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FakeContext extends GenericContext {
    private String path;

    @Override
    public GenericObject getInitObject() {
        return null;
    }
}
