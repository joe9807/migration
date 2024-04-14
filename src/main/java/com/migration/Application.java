package com.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "30");
        System.setProperty("reactor.ipc.netty.ioSelectCount", "30");
        SpringApplication.run(Application.class, args);
    }
}
