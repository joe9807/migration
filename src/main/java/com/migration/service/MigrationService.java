package com.migration.service;

import com.migration.object.GenericObject;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MigrationService {

    public CompletableFuture<List<String>> handle(GenericObject object){
        CompletableFuture<List<String>> futureObject = CompletableFuture.completedFuture(object).thenApplyAsync(this::getWorkerMethod);
        for (GenericObject child:object.getChildren()){
            futureObject = futureObject.thenCombineAsync(handle(child), (r1, r2)-> Stream.concat(r1.stream(), r2.stream()).collect(Collectors.toList()));
        }

        return futureObject;
    }

    private List<String> getWorkerMethod(GenericObject object){
        System.out.println(object.getName());
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> result = new ArrayList<>();
        result.add(object.getName());
        return result;
    }
}
