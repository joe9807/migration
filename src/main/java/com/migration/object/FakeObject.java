package com.migration.object;

import com.migration.context.FakeContext;
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

    private String type;
    private String path;

    private int levelNumber;
    private int containerNumber;
    private int contentNumber;

    @Override
    public GenericObject create() {
        return FakeObject.builder().type(type).path(path).build();
    }

    @Override
    public List<GenericObject> getChildren() {
        if (type.equalsIgnoreCase("CONTENT")) return new ArrayList<>();

        List<GenericObject> children = new ArrayList<>();

        if (levelNumber == context.getLevelsCount()) {
            children.addAll(IntStream.range(0, context.getContentCount())
                    .mapToObj(contentNumber-> {
                        FakeObject child = FakeObject.builder().type("CONTENT").contentNumber(contentNumber).levelNumber(levelNumber+1).build();
                        child.setPath(path+"/"+child.getName());
                        return child;
                    })
                    .toList());

            return children;
        }

        children.addAll(IntStream.range(0, context.getContainersCount())
                .mapToObj(containerNumber-> {
                    FakeObject child = FakeObject.builder().type("CONTAINER").containerNumber(containerNumber).levelNumber(levelNumber+1).build();
                    child.setPath(path+"/"+child.getName());
                    return child;
                })
                .toList());

        children.addAll(IntStream.range(0, context.getContentCount())
                .mapToObj(contentNumber-> {
                    FakeObject child = FakeObject.builder().type("CONTENT").contentNumber(contentNumber).levelNumber(levelNumber+1).build();
                    child.setPath(path+"/"+child.getName());
                    return child;
                })
                .toList());

        return children;
    }

    @Override
    public String getId() {
        return String.format("%s;%s", levelNumber, (type.equalsIgnoreCase("CONTENT")?contentNumber:containerNumber));
    }

    @Override
    public String getName() {
        if (type.equalsIgnoreCase("CONTAINER")) {
            return String.format("level - %s; container - %s", levelNumber, containerNumber);
        } else {
            return String.format("level - %s; content - %s.txt", levelNumber, contentNumber);
        }
    }

    @Override
    public String getType() {
        return type;
    }
}
