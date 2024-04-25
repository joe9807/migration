package com.migration.object;

import com.migration.context.FakeContext;
import com.migration.enums.MigrationObjectType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class FakeObject extends GenericObject{
    private FakeContext context;

    private String path;

    private int levelNumber;
    private int containerNumber;
    private int contentNumber;

    @Override
    public GenericObject create() {
        FakeObject object = FakeObject.builder().path(path).build();
        object.setType(type);
        return object;
    }

    @Override
    public List<GenericObject> getChildren() {
        if (type == MigrationObjectType.CONTENT) return new ArrayList<>();

        List<GenericObject> children = new ArrayList<>();

        if (levelNumber == context.getLevelsCount()) {
            children.addAll(IntStream.range(0, context.getContentCount())
                    .mapToObj(contentNumber-> {
                        FakeObject child = FakeObject.builder().contentNumber(contentNumber).levelNumber(levelNumber+1).build();
                        child.setType(MigrationObjectType.CONTENT);
                        child.setPath(path+"/"+child.getName());
                        return child;
                    })
                    .toList());

            return children;
        }

        children.addAll(IntStream.range(0, context.getContainersCount())
                .mapToObj(containerNumber-> {
                    FakeObject child = FakeObject.builder().containerNumber(containerNumber).levelNumber(levelNumber+1).build();
                    child.setType(MigrationObjectType.CONTAINER);
                    child.setPath(path+"/"+child.getName());
                    return child;
                })
                .toList());

        children.addAll(IntStream.range(0, context.getContentCount())
                .mapToObj(contentNumber-> {
                    FakeObject child = FakeObject.builder().contentNumber(contentNumber).levelNumber(levelNumber+1).build();
                    child.setType(MigrationObjectType.CONTENT);
                    child.setPath(path+"/"+child.getName());
                    return child;
                })
                .toList());

        return children;
    }

    @Override
    public String getId() {
        return String.format("%s;%s", levelNumber, (type == MigrationObjectType.CONTENT?contentNumber:containerNumber));
    }

    @Override
    public String getName() {
        if (type == MigrationObjectType.CONTAINER) {
            return String.format("level - %s; container - %s", levelNumber, containerNumber);
        } else {
            return String.format("level - %s; content - %s.txt", levelNumber, contentNumber);
        }
    }
}
