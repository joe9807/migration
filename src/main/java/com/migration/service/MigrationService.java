package com.migration.service;

import com.migration.object.GenericObject;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class MigrationService {

    public CompletableFuture<List<String>> handle(GenericObject object){
        List<CompletableFuture<List<String>>> futures = Stream.of(CompletableFuture.completedFuture(object).thenApplyAsync(this::getWorkerMethod).thenApplyAsync(List::of)).collect(Collectors.toList());
        futures.addAll(object.getChildren().stream().map(this::handle).collect(Collectors.toList()));

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApplyAsync(name -> futures.stream().map(CompletableFuture::join).flatMap(Collection::stream).collect(Collectors.toList()));
    }

    private String getWorkerMethod(GenericObject object){
        System.out.println(object.getName());
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return object.getName();
    }
}
