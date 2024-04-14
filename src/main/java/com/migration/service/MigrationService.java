package com.migration.service;

import com.migration.object.GenericObject;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

@Component
public class MigrationService {

    public CompletableFuture<Stream<String>> handle(GenericObject object){
        CompletableFuture<Stream<String>> futureObject = CompletableFuture.completedFuture(object).thenApplyAsync(this::getWorkerMethod);
        for (GenericObject child:object.getChildren()){
            futureObject = futureObject.thenCombineAsync(handle(child), Stream::concat);
        }

        return futureObject;
    }

    private Stream<String> getWorkerMethod(GenericObject object){
        System.out.println(object.getName());
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Stream.of(object.getName());
    }
}
