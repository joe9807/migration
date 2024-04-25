package com.migration.context;

import com.migration.object.FakeObject;
import com.migration.object.GenericObject;
import lombok.*;
import java.text.DecimalFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FakeContext extends GenericContext {
    private int levelsCount;
    private int containersCount;
    private int contentCount;

    @Override
    public GenericObject getInitObject() {
        FakeObject initObject = FakeObject.builder().type("CONTAINER").context(this).build();
        initObject.setPath(initObject.getName());
        return initObject;
    }

    public static FakeContext getFakeContextExample(){
        return FakeContext.builder()
                .levelsCount(10)
                .containersCount(10)
                .contentCount(10)
                .build();
    }

    @Override
    public GenericObject getObject(String id, String path, String type) {
        FakeObject fakeObject = FakeObject.builder().type(type).path(path).context(this).build();

        if (id != null) {
            int levelNumber = Integer.parseInt(id.split(";")[0]);
            int number = Integer.parseInt(id.split(";")[1]);

            fakeObject.setLevelNumber(levelNumber);
            if (type.equalsIgnoreCase("CONTAINER")) {
                fakeObject.setContainerNumber(number);
            } else {
                fakeObject.setContentNumber(number);
            }
        }
        return fakeObject;
    }

    public String toString(){
        long result = 0;
        for (int level=0;level<=levelsCount;level++){
            double degree = Math.pow(containersCount, level);
            result+=(degree+degree*contentCount);
        }

        DecimalFormat df = new DecimalFormat("###,###,###,###,###");
        return "FakeContext "+ df.format(result) + " elements";
    }
}
