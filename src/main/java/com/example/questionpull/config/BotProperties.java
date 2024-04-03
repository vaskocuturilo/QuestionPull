package com.example.questionpull.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
public class BotProperties {

    @Value("${bot.name}")
    String name;

    @Value("${bot.token}")
    String token;

    public String getName() {
        return name;
    }

    public String getToken() {
        return token;
    }
}
