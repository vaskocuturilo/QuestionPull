package com.example.questionpull.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Getter
@Configuration
@PropertySource("classpath:application.properties")
public class BotProperties {

    @Value("${bot.name}")
    String name;

    @Value("${bot.token}")
    String token;
}
