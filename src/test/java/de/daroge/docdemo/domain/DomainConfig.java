package de.daroge.docdemo.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(objectMapper.getDeserializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
        return objectMapper;
    }
}
