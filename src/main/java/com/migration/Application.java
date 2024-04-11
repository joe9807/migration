package com.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@SpringBootApplication
@Configuration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
//
//    @Bean
//    public ObjectMapper jacksonObjectMapper() {
//        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
//                .allowIfSubType("com.migration.context.FileSystemContext")
//                .allowIfSubType("com.migration.context.FakeContext")
//                .build();
//        ObjectMapper mapper = new ObjectMapper();
//        mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
//        return mapper;
//    }
}
