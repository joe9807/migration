package com.migration.service;

import com.migration.object.GenericObject;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

    public Flux<String> handleFlux(GenericObject object){
        Flux<String> fluxObject = Flux.just(object).map(this::getWorkerMethod).flatMap(Flux::fromStream);
        for (GenericObject child:object.getChildren()){
            fluxObject = fluxObject.mergeWith(handleFlux(child));
        }

        return fluxObject;
    }

    private Stream<String> getWorkerMethod(GenericObject object){
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(object.getName());
        return Stream.of(object.getName());
    }
}
